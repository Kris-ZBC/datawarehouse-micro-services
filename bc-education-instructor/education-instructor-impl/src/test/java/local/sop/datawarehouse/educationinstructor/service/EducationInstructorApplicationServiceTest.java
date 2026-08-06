package local.sop.datawarehouse.educationinstructor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.educationinstructor.application.api.dto.CreateEducationInstructorCmd;
import local.sop.datawarehouse.educationinstructor.application.api.dto.CreatedEducationInstructorResult;
import local.sop.datawarehouse.educationinstructor.application.api.dto.EducationInstructorResponse;
import local.sop.datawarehouse.educationinstructor.application.api.dto.ToggleActivateEducationInstructorCmd;
import local.sop.datawarehouse.educationinstructor.application.service.EducationInstructorApplicationService;
import local.sop.datawarehouse.educationinstructor.domain.model.EducationInstructor;
import local.sop.datawarehouse.educationinstructor.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.datawarehouse.educationinstructor.domain.ports.out.EducationInstructorRepositoryPort;
import local.sop.datawarehouse.educationinstructor.domain.service.EducationInstructorDomain;

@ExtendWith(MockitoExtension.class)
class EducationInstructorApplicationServiceTest {

    @Mock
    private EducationInstructorRepositoryPort repository;

    @Mock
    private EducationInstructorDomain domain;

    @InjectMocks
    private EducationInstructorApplicationService service;

    private static final UUID EDUCATION_REF = UUID.randomUUID();
    private static final UUID INSTRUCTOR_REF = UUID.randomUUID();
    private static final CompositeKey VALID_KEY = new CompositeKey(EDUCATION_REF, INSTRUCTOR_REF);

    // ── create ─────────────────────────────────────────────────────────────────

    @Test
    void create_shouldSaveAggregateAndReturnResult() {
        EducationInstructor saved = EducationInstructor.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.save(any())).thenReturn(saved);

        CreatedEducationInstructorResult result = service.create(new CreateEducationInstructorCmd(VALID_KEY, true));

        assertNotNull(result);
        assertEquals(VALID_KEY, result.id());
        verify(repository).save(any());
    }

    @Test
    void create_shouldSetActiveFromCommand() {
        EducationInstructor saved = EducationInstructor.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.save(any())).thenReturn(saved);
        when(domain.createEducationInstructor(VALID_KEY, true)).thenReturn(saved);
        

        CreatedEducationInstructorResult result = service.create(new CreateEducationInstructorCmd(VALID_KEY, true));

        assertNotNull(result);
        assertEquals(VALID_KEY, saved.getId());
        verify(repository).save(any());
    }

    // ── toggleActive ───────────────────────────────────────────────────────────

    @Test
    void toggleActive_shouldToggleFromTrueToFalse() {
        EducationInstructor existing = EducationInstructor.builder()
                .id(VALID_KEY)
                .active(false)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));
        when(domain.toggleActivateEducationInstructor(VALID_KEY, true, existing.getCreatedAt().value())).thenReturn(existing);

        EducationInstructorResponse result = service.toggleActive(new ToggleActivateEducationInstructorCmd(VALID_KEY, true));

        assertFalse(result.isActive());
        verify(repository).update(any());
    }

    @Test
    void toggleActive_shouldToggleFromFalseToTrue() {
        EducationInstructor existing = EducationInstructor.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));
        when(domain.toggleActivateEducationInstructor(VALID_KEY, false, existing.getCreatedAt().value())).thenReturn(existing);

        EducationInstructorResponse result = service.toggleActive(new ToggleActivateEducationInstructorCmd(VALID_KEY, false));

        assertTrue(result.isActive());
        verify(repository).update(any());
    }

    @Test
    void toggleActive_shouldThrowNotFoundException_whenEntityDoesNotExist() {
        when(repository.findById(VALID_KEY)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.toggleActive(new ToggleActivateEducationInstructorCmd(VALID_KEY, false)));

        verify(repository, never()).update(any());
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    void getAll_shouldReturnAllResults() {
        EducationInstructor s1 = EducationInstructor.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findAll()).thenReturn(List.of(s1));

        List<EducationInstructorResponse> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals(VALID_KEY, result.get(0).id());
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoResultsExist() {
        when(repository.findAll()).thenReturn(List.of());

        List<EducationInstructorResponse> result = service.getAll();

        assertTrue(result.isEmpty());
    }

    // ── findById ───────────────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnResponse_whenEntityExists() {
        EducationInstructor existing = EducationInstructor.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findById(VALID_KEY)).thenReturn(Optional.of(existing));

        Optional<EducationInstructorResponse> result = service.findById(VALID_KEY);

        assertTrue(result.isPresent());
        assertEquals(VALID_KEY, result.get().id());
        assertTrue(result.get().isActive());
    }

    @Test
    void findById_shouldThrowNotFoundException_whenEntityDoesNotExist() {
        when(repository.findById(VALID_KEY)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findById(VALID_KEY));
    }

    // ── getByEducationRef ────────────────────────────────────────────────────────────

    @Test
    void getByEducationRef_shouldReturnAllMatchingResults() {
        EducationInstructor s1 = EducationInstructor.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findByEducationRef(EDUCATION_REF)).thenReturn(List.of(s1));

        List<EducationInstructorResponse> result = service.getByEducationRef(EDUCATION_REF);

        assertEquals(1, result.size());
        assertEquals(VALID_KEY, result.get(0).id());
    }

    @Test
    void getByEducationRef_shouldReturnEmptyList_whenNoMatchFound() {
        when(repository.findByEducationRef(EDUCATION_REF)).thenReturn(List.of());

        List<EducationInstructorResponse> result = service.getByEducationRef(EDUCATION_REF);

        assertTrue(result.isEmpty());
    }

    // ── getByEInstructorRef ──────────────────────────────────────────────────────

    @Test
    void getByInstructorRef_shouldReturnAllMatchingResults() {
        EducationInstructor s1 = EducationInstructor.builder()
                .id(VALID_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
                .build();

        when(repository.findByInstructorRef(INSTRUCTOR_REF)).thenReturn(List.of(s1));

        List<EducationInstructorResponse> result = service.getByInstructorRef(INSTRUCTOR_REF);

        assertEquals(1, result.size());
        assertEquals(VALID_KEY, result.get(0).id());
    }

    @Test
    void getByInstructorRef_shouldReturnEmptyList_whenNoMatchFound() {
        when(repository.findByInstructorRef(INSTRUCTOR_REF)).thenReturn(List.of());

        List<EducationInstructorResponse> result = service.getByInstructorRef(INSTRUCTOR_REF);

        assertTrue(result.isEmpty());
    }

    @Test
    void compensateCreateEducationInstructor_returnsResponse() {
    EducationInstructorDomain domain = Mockito.mock(EducationInstructorDomain.class);
    EducationInstructorRepositoryPort repository = Mockito.mock(EducationInstructorRepositoryPort.class);
    EducationInstructorApplicationService service = new EducationInstructorApplicationService(repository, domain);

    CompositeKey id = new CompositeKey(UUID.randomUUID(), UUID.randomUUID());

    EducationInstructor found = EducationInstructor.builder()
        .id(id)
        .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
        .active(true)
        .build();

    when(repository.findById(id)).thenReturn(Optional.of(found));
    when(repository.compensateCreateEducationInstructor(id, SagaOutcome.COMPENSATE))
        .thenReturn(true);

    ResponseCompensated response = service.compensateCreateEducationInstructor(id,this.getClass(),SagaOutcome.COMPENSATE);

    assertEquals(SagaOutcome.COMPENSATED, response.sagaState());
    assertTrue(response.success());
    }

    @Test
    void compensateActivateEducationInstructor_returnsResponse() {
    EducationInstructorDomain domain = Mockito.mock(EducationInstructorDomain.class);
    EducationInstructorRepositoryPort repository = Mockito.mock(EducationInstructorRepositoryPort.class);
    EducationInstructorApplicationService service = new EducationInstructorApplicationService(repository, domain);

    CompositeKey id = new CompositeKey(UUID.randomUUID(), UUID.randomUUID());

    EducationInstructor found = EducationInstructor.builder()
        .id(id)
        .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
        .active(true)
        .build();

    when(repository.findById(id)).thenReturn(Optional.of(found));
    when(repository.compensateActivateEducationInstructor(id, SagaOutcome.COMPENSATE))
        .thenReturn(true);

    ResponseCompensated response = service.compensateActivateEducationInstructor(id,this.getClass(),SagaOutcome.COMPENSATE);

    assertEquals(SagaOutcome.COMPENSATED, response.sagaState());
    assertTrue(response.success());
    }

    @Test
    void compensateDeactivateEducationInstructor_returnsResponse() {
    EducationInstructorDomain domain = Mockito.mock(EducationInstructorDomain.class);
    EducationInstructorRepositoryPort repository = Mockito.mock(EducationInstructorRepositoryPort.class);
    EducationInstructorApplicationService service = new EducationInstructorApplicationService(repository, domain);

    CompositeKey id = new CompositeKey(UUID.randomUUID(), UUID.randomUUID());

    EducationInstructor found = EducationInstructor.builder()
        .id(id)
        .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(1)))
        .active(false)
        .build();

    when(repository.findById(id)).thenReturn(Optional.of(found));
    when(repository.compensateDeactivateEducationInstructor(id, SagaOutcome.COMPENSATE))
        .thenReturn(true);

    ResponseCompensated response = service.compensateDeactivateEducationInstructor(id,this.getClass(),SagaOutcome.COMPENSATE);

    assertEquals(SagaOutcome.COMPENSATED, response.sagaState());
    assertTrue(response.success());
    }

}
