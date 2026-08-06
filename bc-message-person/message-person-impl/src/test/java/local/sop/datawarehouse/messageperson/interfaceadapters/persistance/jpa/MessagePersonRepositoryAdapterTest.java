package local.sop.datawarehouse.messageperson.interfaceadapters.persistance.jpa;

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

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.datawarehouse.messageperson.domain.model.MessagePerson;
import local.sop.datawarehouse.messageperson.interfaceadapters.persistence.jpa.MessagePersonDomainJpaMapper;
import local.sop.datawarehouse.messageperson.interfaceadapters.persistence.jpa.MessagePersonEntity;
import local.sop.datawarehouse.messageperson.interfaceadapters.persistence.jpa.MessagePersonId;
import local.sop.datawarehouse.messageperson.interfaceadapters.persistence.jpa.MessagePersonRepositoryAdapter;

@DataJpaTest
@TestPropertySource(properties = {
    "security.enabled=false"})
@ActiveProfiles({"test", "h2"})
@Import({MessagePersonRepositoryAdapter.class, MessagePersonDomainJpaMapper.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MessagePersonRepositoryAdapterTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MessagePersonDomainJpaMapper mapper;

    @Autowired
    private MessagePersonRepositoryAdapter adapter;

    // ── Helper ───────────────────────────────────────────────────────────────

    private MessagePersonEntity persistEntity(UUID messageRef, UUID personRef, boolean active) {
        MessagePersonEntity entity = MessagePersonEntity.builder()
                .id(new MessagePersonId(messageRef, personRef))
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
        UUID messageRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        persistEntity(messageRef, personRef, true);

        Optional<MessagePerson> result =
                adapter.findById(new CompositeKey(messageRef, personRef));

        assertTrue(result.isPresent());
        assertEquals(messageRef, result.get().getId().key1());
        assertEquals(personRef, result.get().getId().key2());
    }

    @Test
    void findById_shouldReturnEmpty_whenEntityDoesNotExist() {
        Optional<MessagePerson> result =
                adapter.findById(new CompositeKey(UUID.randomUUID(), UUID.randomUUID()));

        assertTrue(result.isEmpty());
    }

    // ── save ─────────────────────────────────────────────────────────────────

    @Test
    void save_shouldPersistAndReturnDomain() {
        UUID messageRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        MessagePerson domain = mapper.toDomain(
                MessagePersonEntity.builder()
                        .id(new MessagePersonId(messageRef, personRef))
                        .active(true)
                        .build());

        MessagePerson saved = adapter.save(domain);

        assertNotNull(saved);
        assertEquals(messageRef, saved.getId().key1());
        assertEquals(personRef, saved.getId().key2());
        assertTrue(saved.isActive());
    }

    @Test
    void save_shouldPersistWithCorrectCreatedAt() {
        UUID messageRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        MessagePerson domain = mapper.toDomain(
                MessagePersonEntity.builder()
                        .id(new MessagePersonId(messageRef, personRef))
                        .active(true)
                        .build());

        MessagePerson saved = adapter.save(domain);

        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertNotNull(saved.getCreatedAt());
        assertTrue(saved.getCreatedAt().value().isAfter(before));
        assertTrue(saved.getCreatedAt().value().isBefore(after));
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_shouldUpdateActive_whenEntityExists() {
        UUID messageRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        persistEntity(messageRef, personRef, true);
        entityManager.clear();

        MessagePerson updated = mapper.toDomain(
                MessagePersonEntity.builder()
                        .id(new MessagePersonId(messageRef, personRef))
                        .active(false)
                        .build());

        adapter.update(updated);
        entityManager.flush();
        entityManager.clear();

        Optional<MessagePerson> result =
                adapter.findById(new CompositeKey(messageRef, personRef));

        assertTrue(result.isPresent());
        assertFalse(result.get().isActive());
    }

    @Test
    void update_shouldThrowNotFoundException_whenEntityDoesNotExist() {
        MessagePerson domain = mapper.toDomain(
                MessagePersonEntity.builder()
                        .id(new MessagePersonId(UUID.randomUUID(), UUID.randomUUID()))
                        .active(false)
                        .build());

        assertThrows(NotFoundException.class, () -> adapter.update(domain));
    }

    // ── findByMessageRef ─────────────────────────────────────────────────────

    @Test
    void findByMessageRef_shouldReturnAllMatchingEntities() {
        UUID messageRef1 = UUID.randomUUID();
        UUID messageRef2 = UUID.randomUUID();
        UUID personRef1 = UUID.randomUUID();
        UUID personRef2 = UUID.randomUUID();

        persistEntity(messageRef1, personRef1, true);
        persistEntity(messageRef1, personRef2, false);
        persistEntity(messageRef2, personRef1, true);

        List<MessagePerson> result = adapter.findByMessageRef(messageRef1);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(mp -> mp.getId().key1().equals(messageRef1)));
    }

    @Test
    void findByMessageRef_shouldReturnEmptyList_whenNoMatchesFound() {
        UUID messageRef1 = UUID.randomUUID();
        UUID messageRef2 = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        persistEntity(messageRef1, personRef, true);

        List<MessagePerson> result = adapter.findByMessageRef(messageRef2);

        assertTrue(result.isEmpty());
    }

    // ── findByPersonRef ──────────────────────────────────────────────────────

    @Test
    void findByPersonRef_shouldReturnAllMatchingEntities() {
        UUID messageRef1 = UUID.randomUUID();
        UUID messageRef2 = UUID.randomUUID();
        UUID personRef1 = UUID.randomUUID();
        UUID personRef2 = UUID.randomUUID();

        persistEntity(messageRef1, personRef1, true);
        persistEntity(messageRef2, personRef1, false);
        persistEntity(messageRef1, personRef2, true);

        List<MessagePerson> result = adapter.findByPersonRef(personRef1);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(mp -> mp.getId().key2().equals(personRef1)));
    }

    @Test
    void findByPersonRef_shouldReturnEmptyList_whenNoMatchesFound() {
        UUID messageRef = UUID.randomUUID();
        UUID personRef1 = UUID.randomUUID();
        UUID personRef2 = UUID.randomUUID();

        persistEntity(messageRef, personRef1, true);

        List<MessagePerson> result = adapter.findByPersonRef(personRef2);

        assertTrue(result.isEmpty());
    }
}