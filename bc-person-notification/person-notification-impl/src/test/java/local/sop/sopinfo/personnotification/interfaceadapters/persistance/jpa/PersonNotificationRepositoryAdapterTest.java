package local.sop.sopinfo.personnotification.interfaceadapters.persistance.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import local.sop.sopinfo.personnotification.domain.model.PersonNotification;
import local.sop.sopinfo.personnotification.interfaceadapters.persistence.jpa.PersonNotificationJpaMapper;
import local.sop.sopinfo.personnotification.interfaceadapters.persistence.jpa.PersonNotificationEntity;
import local.sop.sopinfo.personnotification.interfaceadapters.persistence.jpa.PersonNotificationId;
import local.sop.sopinfo.personnotification.interfaceadapters.persistence.jpa.PersonNotificationRepositoryAdapter;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;

@DataJpaTest
@TestPropertySource(properties = {
    "security.enabled=false"})
@ActiveProfiles({"test", "h2"})
@Import({PersonNotificationRepositoryAdapter.class, PersonNotificationJpaMapper.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PersonNotificationRepositoryAdapterTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PersonNotificationJpaMapper mapper;

    @Autowired
    private PersonNotificationRepositoryAdapter adapter;

    // ── Helper ───────────────────────────────────────────────────────────────

    private PersonNotificationEntity persistEntity(UUID personRef, UUID notificationRef, boolean active) {
        PersonNotificationEntity entity = PersonNotificationEntity.builder()
                .id(new PersonNotificationId(personRef, notificationRef))
                .active(active)
                .build();

        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();
        return entity;
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnDomain_whenEntityExists() {
        UUID notificationRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        persistEntity(personRef, notificationRef, true);

        Optional<PersonNotification> result =
                adapter.findById(new CompositeKey(personRef, notificationRef));

        assertTrue(result.isPresent());
        assertEquals(personRef, result.get().getId().key1());
        assertEquals(notificationRef, result.get().getId().key2());
    }

    @Test
    void findById_shouldReturnEmpty_whenEntityDoesNotExist() {
        Optional<PersonNotification> result =
                adapter.findById(new CompositeKey(UUID.randomUUID(), UUID.randomUUID()));

        assertTrue(result.isEmpty());
    }

    // ── save ─────────────────────────────────────────────────────────────────

    @Test
    void save_shouldPersistAndReturnDomain() {
        UUID notificationRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        PersonNotification domain = mapper.toDomain(
                PersonNotificationEntity.builder()
                        .id(new PersonNotificationId(personRef, notificationRef))
                        .active(true)
                        .build());

        PersonNotification saved = adapter.save(domain);

        assertNotNull(saved);
        assertEquals(personRef, saved.getId().key1());
        assertEquals(notificationRef, saved.getId().key2());
        assertTrue(saved.isActive());
    }

    @Test
    void save_shouldPersistWithCorrectCreatedAt() {
        UUID notificationRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        PersonNotification domain = mapper.toDomain(
                PersonNotificationEntity.builder()
                        .id(new PersonNotificationId(personRef, notificationRef))
                        .active(true)
                        .build());

        PersonNotification saved = adapter.save(domain);

        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertNotNull(saved.getCreatedAt());
        assertTrue(saved.getCreatedAt().value().isAfter(before));
        assertTrue(saved.getCreatedAt().value().isBefore(after));
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_shouldUpdateActive_whenEntityExists() {
        UUID notificationRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        persistEntity(personRef, notificationRef, true);
        entityManager.clear();

        PersonNotification updated = mapper.toDomain(
                PersonNotificationEntity.builder()
                        .id(new PersonNotificationId(personRef, notificationRef))
                        .active(false)
                        .build());

        adapter.update(updated);
        entityManager.flush();
        entityManager.clear();

        Optional<PersonNotification> result =
                adapter.findById(new CompositeKey(personRef, notificationRef));

        assertTrue(result.isPresent());
        assertFalse(result.get().isActive());
    }

    @Test
    void update_shouldThrowNotFoundException_whenEntityDoesNotExist() {
        PersonNotification domain = mapper.toDomain(
                PersonNotificationEntity.builder()
                        .id(new PersonNotificationId(UUID.randomUUID(), UUID.randomUUID()))
                        .active(false)
                        .build());

        assertThrows(NotFoundException.class, () -> adapter.update(domain));
    }

    // ── findByPersonRef ─────────────────────────────────────────────────────

    @Test
    void findByPersonRef_shouldReturnAllMatchingEntities() {
        UUID personRef1 = UUID.randomUUID();
        UUID personRef2 = UUID.randomUUID();
        UUID notificationRef1 = UUID.randomUUID();
        UUID notificationRef2 = UUID.randomUUID();

        persistEntity(personRef1, notificationRef1, true);
        persistEntity(personRef1, notificationRef2, false);
        persistEntity(personRef2, notificationRef1, true);

        List<PersonNotification> result = adapter.findByPersonRef(personRef1);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(mp -> mp.getId().key1().equals(personRef1)));
    }

    @Test
    void findByPersonRef_shouldReturnEmptyList_whenNoMatchesFound() {
        UUID personRef1 = UUID.randomUUID();
        UUID personRef2 = UUID.randomUUID();
        UUID notificationRef = UUID.randomUUID();

        persistEntity(personRef1, notificationRef, true);

        List<PersonNotification> result = adapter.findByPersonRef(personRef2);

        assertTrue(result.isEmpty());
    }

    // ── findByNotificationRef ──────────────────────────────────────────────────────

    @Test
    void findByNotificationRef_shouldReturnAllMatchingEntities() {
        UUID personRef1 = UUID.randomUUID();
        UUID personRef2 = UUID.randomUUID();
        UUID notificationRef1 = UUID.randomUUID();
        UUID notificationRef2 = UUID.randomUUID();

        persistEntity(personRef1, notificationRef1, true);
        persistEntity(personRef2, notificationRef1, false);
        persistEntity(personRef1, notificationRef2, true);

        List<PersonNotification> result = adapter.findByNotificationRef(notificationRef1);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(mp -> mp.getId().key2().equals(notificationRef1)));
    }

    @Test
    void findByNotificationRef_shouldReturnEmptyList_whenNoMatchesFound() {
        UUID personRef = UUID.randomUUID();
        UUID notificationRef1 = UUID.randomUUID();
        UUID notificationRef2 = UUID.randomUUID();

        persistEntity(personRef, notificationRef1, true);

        List<PersonNotification> result = adapter.findByNotificationRef(notificationRef2);

        assertTrue(result.isEmpty());
    }
}
