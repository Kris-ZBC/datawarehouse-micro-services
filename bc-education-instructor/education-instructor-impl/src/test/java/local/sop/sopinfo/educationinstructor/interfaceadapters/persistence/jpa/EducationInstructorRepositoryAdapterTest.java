package local.sop.sopinfo.educationinstructor.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import local.sop.sopinfo.educationinstructor.domain.model.EducationInstructor;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sharedkernel.exceptions.NotFoundException;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

@DataJpaTest
@TestPropertySource(properties = {
    "security.enabled=false"})
@ActiveProfiles({"test", "h2"})
@Import({EducationInstructorRepositoryAdapter.class, EducationInstructorJpaMapper.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class EducationInstructorRepositoryAdapterTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EducationInstructorJpaMapper mapper;

    @Autowired
    private EducationInstructorRepositoryAdapter adapter;

    // ── Helper ───────────────────────────────────────────────────────────────

    private EducationInstructorEntity persistEntity(UUID educationRef,UUID instructorRef,boolean active) {
    EducationInstructorEntity entity = EducationInstructorEntity.builder()
            .id(new EducationInstructorId(educationRef, instructorRef))
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
        UUID educationRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();

        persistEntity(educationRef, instructorRef, true);

        Optional<EducationInstructor> result =
                adapter.findById(new CompositeKey(educationRef, instructorRef));

        assertTrue(result.isPresent());
        assertEquals(educationRef, result.get().getId().key1());
        assertEquals(instructorRef, result.get().getId().key2());
    }

    @Test
    void findById_shouldReturnEmpty_whenEntityDoesNotExist() {
        Optional<EducationInstructor> result =
                adapter.findById(new CompositeKey(UUID.randomUUID(), UUID.randomUUID()));

        assertTrue(result.isEmpty());
    }

    // ── save ─────────────────────────────────────────────────────────────────

    @Test
    void save_shouldPersistAndReturnDomain() {
        UUID educationRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();

        EducationInstructor domain = mapper.toDomain(
                EducationInstructorEntity.builder()
                        .id(new EducationInstructorId(educationRef, instructorRef))
                        .active(true)
                        .build());

        EducationInstructor saved = adapter.save(domain);

        assertNotNull(saved);
        assertEquals(educationRef, saved.getId().key1());
        assertEquals(instructorRef, saved.getId().key2());
        assertTrue(saved.isActive());
    }

    @Test
    void save_shouldPersistWithCorrectCreatedAt() {
        UUID educationRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();

        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        EducationInstructor domain = mapper.toDomain(
                EducationInstructorEntity.builder()
                        .id(new EducationInstructorId(educationRef, instructorRef))
                        .active(true)
                        .build());

        EducationInstructor saved = adapter.save(domain);

        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertNotNull(saved.getCreatedAt());
        assertTrue(saved.getCreatedAt().value().isAfter(before));
        assertTrue(saved.getCreatedAt().value().isBefore(after));
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_shouldUpdateActive_whenEntityExists() {
        UUID educationRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();

        persistEntity(educationRef, instructorRef, true);
        entityManager.clear();

        EducationInstructor updated = mapper.toDomain(
                EducationInstructorEntity.builder()
                        .id(new EducationInstructorId(educationRef, instructorRef))
                        .active(false)
                        .build());

        adapter.update(updated);
        entityManager.flush();
        entityManager.clear();

        Optional<EducationInstructor> result =
                adapter.findById(new CompositeKey(educationRef, instructorRef));

        assertTrue(result.isPresent());
        assertFalse(result.get().isActive());
    }

    @Test
    void update_shouldThrowNotFoundException_whenEntityDoesNotExist() {
        EducationInstructor domain = mapper.toDomain(
                EducationInstructorEntity.builder()
                        .id(new EducationInstructorId(UUID.randomUUID(), UUID.randomUUID()))
                        .active(false)
                        .build());

        assertThrows(NotFoundException.class, () -> adapter.update(domain));
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    void findAll_shouldReturnAllEntities() {
        UUID educationRef1 = UUID.randomUUID();
        UUID educationRef2 = UUID.randomUUID();
        UUID instructorRef1 = UUID.randomUUID();
        UUID instructorRef2 = UUID.randomUUID();

        persistEntity(educationRef1, instructorRef1, true);
        persistEntity(educationRef2, instructorRef2, false);

        List<EducationInstructor> result = adapter.findAll();

        assertEquals(2, result.size());
    }

    // ── findByEducationRef ─────────────────────────────────────────────────────

    @Test
    void findByEducationRef_shouldReturnAllMatchingEntities() {
        UUID educationRef1 = UUID.randomUUID();
        UUID educationRef2 = UUID.randomUUID();
        UUID instructorRef1 = UUID.randomUUID();
        UUID instructorRef2 = UUID.randomUUID();

        persistEntity(educationRef1, instructorRef1, true);
        persistEntity(educationRef1, instructorRef2, false);
        persistEntity(educationRef2, instructorRef1, true);

        List<EducationInstructor> result = adapter.findByEducationRef(educationRef1);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(mp -> mp.getId().key1().equals(educationRef1)));
    }

    @Test
    void findByEducationRef_shouldReturnEmptyList_whenNoMatchesFound() {
        UUID educationRef1 = UUID.randomUUID();
        UUID educationRef2 = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();

        persistEntity(educationRef1, instructorRef, true);

        List<EducationInstructor> result = adapter.findByEducationRef(educationRef2);

        assertTrue(result.isEmpty());
    }

    // ── findByInstructorRef ──────────────────────────────────────────────────────

    @Test
    void findByInstructorRef_shouldReturnAllMatchingEntities() {
        UUID educationRef1 = UUID.randomUUID();
        UUID educationRef2 = UUID.randomUUID();
        UUID instructorRef1 = UUID.randomUUID();
        UUID instructorRef2 = UUID.randomUUID();

        persistEntity(educationRef1, instructorRef1, true);
        persistEntity(educationRef2, instructorRef1, false);
        persistEntity(educationRef1, instructorRef2, true);

        List<EducationInstructor> result = adapter.findByInstructorRef(instructorRef1);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(mp -> mp.getId().key2().equals(instructorRef1)));
    }

    @Test
    void findByInstructorRef_shouldReturnEmptyList_whenNoMatchesFound() {
        UUID educationRef = UUID.randomUUID();
        UUID instructorRef1 = UUID.randomUUID();
        UUID instructorRef2 = UUID.randomUUID();

        persistEntity(educationRef, instructorRef1, true);

        List<EducationInstructor> result = adapter.findByInstructorRef(instructorRef2);

        assertTrue(result.isEmpty());
    }

    @Test
    void compensateCreateEducationInstructor_deletesEntity() {
        EducationInstructorSpringDataRepository springRepo = Mockito.mock(EducationInstructorSpringDataRepository.class);
        EducationInstructorJpaMapper mapper = new EducationInstructorJpaMapper();
        EducationInstructorRepositoryAdapter adapter = new EducationInstructorRepositoryAdapter(springRepo,mapper);
        EducationInstructorId jpaId = new EducationInstructorId(UUID.randomUUID(),UUID.randomUUID());
        CompositeKey id = new CompositeKey(jpaId.getEducationRef(), jpaId.getInstructorRef());

        when(springRepo.findById(any(EducationInstructorId.class)))
            .thenReturn(Optional.of(new EducationInstructorEntity(jpaId, true)));

        Boolean result = adapter.compensateCreateEducationInstructor(id, SagaOutcome.COMPENSATE);

        assertTrue(result);
        verify(springRepo).delete(jpaId.getEducationRef(), jpaId.getInstructorRef());
    }

    @Test
    void compensateActivateEducationInstructor_callsCompensateActivate() {
        EducationInstructorSpringDataRepository springRepo = Mockito.mock(EducationInstructorSpringDataRepository.class);
        EducationInstructorJpaMapper mapper = new EducationInstructorJpaMapper();
        EducationInstructorRepositoryAdapter adapter = new EducationInstructorRepositoryAdapter( springRepo, mapper);
        EducationInstructorId jpaId = new EducationInstructorId(UUID.randomUUID(), UUID.randomUUID());
        CompositeKey id = new CompositeKey(jpaId.getEducationRef(), jpaId.getInstructorRef());

        when(springRepo.findById(any(EducationInstructorId.class)))
            .thenReturn(Optional.of( new EducationInstructorEntity(jpaId, true)));

        Boolean result = adapter.compensateActivateEducationInstructor(id, SagaOutcome.COMPENSATE);

        assertTrue(result);
        verify(springRepo).compensateActivateByEducationRefAndInstructorRef(jpaId.getEducationRef(), jpaId.getInstructorRef());
    }

    @Test
    void compensateDeactivateEducationInstructor_callsCompensateDeactivate() {
        EducationInstructorSpringDataRepository springRepo = Mockito.mock(EducationInstructorSpringDataRepository.class);
        EducationInstructorJpaMapper mapper = new EducationInstructorJpaMapper();
        EducationInstructorRepositoryAdapter adapter = new EducationInstructorRepositoryAdapter(springRepo, mapper);
        EducationInstructorId jpaId = new EducationInstructorId(UUID.randomUUID(), UUID.randomUUID());
        CompositeKey id = new CompositeKey(jpaId.getEducationRef(), jpaId.getInstructorRef());

        when(springRepo.findById(any(EducationInstructorId.class)))
            .thenReturn(Optional.of(new EducationInstructorEntity(jpaId, false)));

        Boolean result = adapter.compensateDeactivateEducationInstructor(id, SagaOutcome.COMPENSATE);

        assertTrue(result);
        verify(springRepo).compensateDeactivateByEducationRefAndInstructorRef(jpaId.getEducationRef(), jpaId.getInstructorRef());
    }

}
