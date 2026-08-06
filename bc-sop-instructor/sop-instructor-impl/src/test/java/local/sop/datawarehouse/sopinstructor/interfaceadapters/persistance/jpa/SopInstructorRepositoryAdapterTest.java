package local.sop.datawarehouse.sopinstructor.interfaceadapters.persistance.jpa;

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
import local.sop.datawarehouse.sopinstructor.domain.model.SopInstructor;
import local.sop.datawarehouse.sopinstructor.interfaceadapters.persistence.jpa.SopInstructorDomainJpaMapper;
import local.sop.datawarehouse.sopinstructor.interfaceadapters.persistence.jpa.SopInstructorEntity;
import local.sop.datawarehouse.sopinstructor.interfaceadapters.persistence.jpa.SopInstructorId;
import local.sop.datawarehouse.sopinstructor.interfaceadapters.persistence.jpa.SopInstructorRepositoryAdapter;

@DataJpaTest
@TestPropertySource(properties = {
    "security.enabled=false"})
@ActiveProfiles({"test", "h2"})
@Import({SopInstructorRepositoryAdapter.class, SopInstructorDomainJpaMapper.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SopInstructorRepositoryAdapterTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SopInstructorDomainJpaMapper mapper;

    @Autowired
    private SopInstructorRepositoryAdapter adapter;

    // ── Helper ───────────────────────────────────────────────────────────────

    private SopInstructorEntity persistEntity(UUID sopRef, UUID instructorRef, boolean active) {
        SopInstructorEntity entity = SopInstructorEntity.builder()
                .id(new SopInstructorId(sopRef, instructorRef))
                .active(active)
                .build();

        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();
        return entity;
    }

    // ── findById ─────────────────────────────────────────────────────────────

    // @Test
    // void findById_shouldReturnDomain_whenEntityExists() {
    //     UUID sopRef = UUID.randomUUID();
    //     UUID instructorRef = UUID.randomUUID();

    //     persistEntity(sopRef, instructorRef, true);

    //     Optional<SopInstructor> result =
    //             adapter.findById(new CompositeKey(sopRef, instructorRef));

    //     assertTrue(result.isPresent());
    //     assertEquals(sopRef, result.get().getId().key1());
    //     assertEquals(instructorRef, result.get().getId().key2());
    // }

    @Test
    void findById_shouldReturnEmpty_whenEntityDoesNotExist() {
        Optional<SopInstructor> result =
                adapter.findById(new CompositeKey(UUID.randomUUID(), UUID.randomUUID()));

        assertTrue(result.isEmpty());
    }

    // ── save ─────────────────────────────────────────────────────────────────

    @Test
    void save_shouldPersistAndReturnDomain() {
        UUID sopRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();

        SopInstructor domain = mapper.toDomain(
                SopInstructorEntity.builder()
                        .id(new SopInstructorId(sopRef, instructorRef))
                        .active(true)
                        .build());

        SopInstructor saved = adapter.save(domain);

        assertNotNull(saved);
        assertEquals(sopRef, saved.getId().key1());
        assertEquals(instructorRef, saved.getId().key2());
        assertTrue(saved.isActive());
    }

    @Test
    void save_shouldPersistWithCorrectCreatedAt() {
        UUID sopRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();

        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        SopInstructor domain = mapper.toDomain(
                SopInstructorEntity.builder()
                        .id(new SopInstructorId(sopRef, instructorRef))
                        .active(true)
                        .build());

        SopInstructor saved = adapter.save(domain);

        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertNotNull(saved.getCreatedAt());
        assertTrue(saved.getCreatedAt().value().isAfter(before));
        assertTrue(saved.getCreatedAt().value().isBefore(after));
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_shouldUpdateActive_whenEntityExists() {
        UUID sopRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();

        persistEntity(sopRef, instructorRef, true);
        entityManager.clear();

        SopInstructor updated = mapper.toDomain(
                SopInstructorEntity.builder()
                        .id(new SopInstructorId(sopRef, instructorRef))
                        .active(false)
                        .build());

        adapter.update(updated);
        entityManager.flush();
        entityManager.clear();

        Optional<SopInstructor> result =
                adapter.findById(new CompositeKey(sopRef, instructorRef));

        assertTrue(result.isPresent());
        assertFalse(result.get().isActive());
    }

    @Test
    void update_shouldThrowNotFoundException_whenEntityDoesNotExist() {
        SopInstructor domain = mapper.toDomain(
                SopInstructorEntity.builder()
                        .id(new SopInstructorId(UUID.randomUUID(), UUID.randomUUID()))
                        .active(false)
                        .build());

        assertThrows(NotFoundException.class, () -> adapter.update(domain));
    }

    // ── findBySopRef ─────────────────────────────────────────────────────

    @Test
    void findBySopRef_shouldReturnAllMatchingEntities() {
        UUID sopRef1 = UUID.randomUUID();
        UUID sopRef2 = UUID.randomUUID();
        UUID instructorRef1 = UUID.randomUUID();
        UUID instructorRef2 = UUID.randomUUID();

        persistEntity(sopRef1, instructorRef1, true);
        persistEntity(sopRef1, instructorRef2, false);
        persistEntity(sopRef2, instructorRef1, true);

        List<SopInstructor> result = adapter.findBySopRef(sopRef1);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(mp -> mp.getId().key1().equals(sopRef1)));
    }

    @Test
    void findBySopRef_shouldReturnEmptyList_whenNoMatchesFound() {
        UUID sopRef1 = UUID.randomUUID();
        UUID sopRef2 = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();

        persistEntity(sopRef1, instructorRef, true);

        List<SopInstructor> result = adapter.findBySopRef(sopRef2);

        assertTrue(result.isEmpty());
    }

    // ── findByInstructorRef ──────────────────────────────────────────────────────

    @Test
    void findByInstructorRef_shouldReturnAllMatchingEntities() {
        UUID sopRef1 = UUID.randomUUID();
        UUID sopRef2 = UUID.randomUUID();
        UUID instructorRef1 = UUID.randomUUID();
        UUID instructorRef2 = UUID.randomUUID();

        persistEntity(sopRef1, instructorRef1, true);
        persistEntity(sopRef2, instructorRef1, false);
        persistEntity(sopRef1, instructorRef2, true);

        List<SopInstructor> result = adapter.findByInstructorRef(instructorRef1);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(mp -> mp.getId().key2().equals(instructorRef1)));
    }

    @Test
    void findByInstructorRef_shouldReturnEmptyList_whenNoMatchesFound() {
        UUID sopRef = UUID.randomUUID();
        UUID instructorRef1 = UUID.randomUUID();
        UUID instructorRef2 = UUID.randomUUID();

        persistEntity(sopRef, instructorRef1, true);

        List<SopInstructor> result = adapter.findByInstructorRef(instructorRef2);

        assertTrue(result.isEmpty());
    }
}