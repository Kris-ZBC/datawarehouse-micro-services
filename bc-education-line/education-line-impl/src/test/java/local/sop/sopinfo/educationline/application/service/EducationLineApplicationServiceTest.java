package local.sop.sopinfo.educationline.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import local.sop.sopinfo.educationline.application.api.dto.CreateEducationLineCmd;
import local.sop.sopinfo.educationline.application.api.dto.EducationLineResponse;
import local.sop.sopinfo.educationline.application.api.dto.UpdateEducationLineDurationCmd;
import local.sop.sopinfo.educationline.application.api.dto.UpdateEducationLineNameCmd;
import local.sop.sopinfo.educationline.domain.model.EducationLine;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineCreatedAt;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineDuration;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineId;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineName;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationRef;
import local.sop.sopinfo.educationline.domain.ports.out.EducationLineRepositoryPort;
import local.sop.sopinfo.educationline.domain.service.EducationLineDomain;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;

class EducationLineApplicationServiceTest {

    private final UUID compensateId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private EducationLineDomain domain;
    private EducationLineRepositoryPort repository;
    private EducationLineApplicationService service;
    private static EducationLine aggregate(UUID id, String name, boolean active) {
        return EducationLine.builder()
                .id(new EducationLineId(id))
                .name(new EducationLineName(name))
                .duration(new EducationLineDuration(1, 2, 3))
                .createdAt(new EducationLineCreatedAt(Instant.parse("2026-03-16T10:00:00Z")))
                .educationRef(new EducationRef(UUID.fromString("11111111-1111-1111-1111-111111111111")))
                .active(active)
                .build();
    }

    @BeforeEach
    void setup() {
        domain = Mockito.mock(EducationLineDomain.class);
        repository = Mockito.mock(EducationLineRepositoryPort.class);
        service = new EducationLineApplicationService(domain, repository);
    }

    @Test
    void createEducationLine_maps_and_returns_response() {

        UUID id = UUID.randomUUID();
        EducationLine created = aggregate(id, "Mathematics", true);
        when(domain.createEducationLine(any(EducationLine.class))).thenReturn(created);
        when(repository.save(any(EducationLine.class))).thenReturn(created);

        EducationLineResponse result = service.createEducationLine(
            new CreateEducationLineCmd("Mathematics", 1, 2, 3, UUID.fromString("11111111-1111-1111-1111-111111111111"))
        );

        assertEquals(id, result.id());
        assertEquals("Mathematics", result.name());
        assertTrue(result.isActive());
    }

    @Test
    void findAll_returns_mapped_responses() {
        when(repository.findAll()).thenReturn(List.of(
            aggregate(UUID.randomUUID(), "Math", true),
            aggregate(UUID.randomUUID(), "Physics", false)
        ));

        List<EducationLineResponse> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals("Math", result.get(0).name());
        assertFalse(result.get(1).isActive());
    }

    @Test
    void findById_returns_response_when_found() {
        UUID id = UUID.randomUUID();
        when(repository.findById(any(EducationLineId.class))).thenReturn(Optional.of(aggregate(id, "Math", true)));

        EducationLineResponse result = service.findById(id).get();

        assertEquals(id, result.id());
    }

    @Test
    void findById_throws_not_found_when_missing() {
        when(repository.findById(any(EducationLineId.class))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findById(UUID.randomUUID()));
    }


	@Test
	void findByEducationRef_returns_response_when_found() {
		UUID id = UUID.randomUUID();
		EducationRef educationRef = new EducationRef(UUID.randomUUID());
		when(repository.findByEducationRef(any(EducationRef.class))).thenReturn(List.of(aggregate(id, "Math", true)));
	
	    List<EducationLineResponse> result = service.findByEducationRef(educationRef.value());

		assertFalse(result.isEmpty());
		assertEquals(id, result.get(0).id());
		assertEquals("Math", result.get(0).name());
	}
	@Test 
	void findByEducationRef_throws_not_found_when_missing() {
		when(repository.findByEducationRef(any(EducationRef.class))).thenReturn(List.of());

		assertThrows(NotFoundException.class, () -> service.findByEducationRef(UUID.randomUUID()));
	}

    @Test
    void update_name_and_duration_return_updated_or_throw_when_missing() {
        UUID id = UUID.randomUUID();
        when(repository.updateEducationLineName(any(EducationLineId.class), any(EducationLineName.class)))
            .thenReturn(Optional.of(aggregate(id, "New Name", true)));
        when(repository.updateEducationLineDuration(any(EducationLineId.class), any(EducationLineDuration.class)))
            .thenReturn(Optional.of(aggregate(id, "New Name", true)));

        EducationLineResponse byName = service.updateEducationLineName(id, new UpdateEducationLineNameCmd("New Name"));
        EducationLineResponse byDuration = service.updateEducationLineDuration(id, new UpdateEducationLineDurationCmd(1, 2, 3));

        assertEquals("New Name", byName.name());
        assertEquals(1, byDuration.durationYears());

        when(repository.updateEducationLineName(any(EducationLineId.class), any(EducationLineName.class))).thenReturn(Optional.empty());
        when(repository.updateEducationLineDuration(any(EducationLineId.class), any(EducationLineDuration.class))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.updateEducationLineName(id, new UpdateEducationLineNameCmd("X")));
        assertThrows(NotFoundException.class, () -> service.updateEducationLineDuration(id, new UpdateEducationLineDurationCmd(1, 0, 1)));
    }

    @Test
    void deactivate_and_activate_return_updated_or_throw_when_missing() {
        UUID id = UUID.randomUUID();
        when(repository.deactivate(any(EducationLineId.class)))
            .thenReturn(Optional.of(aggregate(id, "Math", false)))
            .thenReturn(Optional.empty());
        when(repository.activate(any(EducationLineId.class)))
            .thenReturn(Optional.of(aggregate(id, "Math", true)))
            .thenReturn(Optional.empty());

        EducationLineResponse deactivated = service.deactivateEducationLine(id);
        EducationLineResponse activated = service.activateEducationLine(id);

        assertFalse(deactivated.isActive());
        assertTrue(activated.isActive());

        assertThrows(NotFoundException.class, () -> service.deactivateEducationLine(id));
        assertThrows(NotFoundException.class, () -> service.activateEducationLine(id));
    }

    @Test
    void compensateEducationLine_WhenEducationLineExistsAndCompensateSucceeds_ShouldReturnCompensated() {
        // Given
        PayloadCompensateCreate cmd = new PayloadCompensateCreate(
            EducationLineApplicationServiceTest.class,
            SagaOutcome.COMPENSATE
        );

        when(repository.findById(any(EducationLineId.class)))
            .thenReturn(Optional.of(aggregate(compensateId, "Math", true)));
        when(repository.compensate(any(EducationLineId.class), any(SagaOutcome.class)))
            .thenReturn(true);

        // When
        ResponseCompensated result = service.compensate(compensateId, cmd.getClass(), cmd.sagaState());

        // Then
        assertEquals(SagaOutcome.COMPENSATED, result.sagaState());
        assertTrue(result.sagaState() == SagaOutcome.COMPENSATED);

        verify(repository).findById(any(EducationLineId.class));
        verify(repository).compensate(any(EducationLineId.class), any(SagaOutcome.class));
    }

    @Test
    void compensateEducationLine_WhenEducationLineDoesNotExist_ShouldReturnIdempotentFalse() {
        // Given
        PayloadCompensateCreate cmd = new PayloadCompensateCreate(
            EducationLineApplicationServiceTest.class,
            SagaOutcome.COMPENSATE
        );

        when(repository.findById(new EducationLineId(compensateId)))
            .thenReturn(Optional.empty());

        // When
        ResponseCompensated result = service.compensate(compensateId, cmd.getClass(), cmd.sagaState());

        // Then
        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertFalse(result.sagaState() == SagaOutcome.COMPENSATED);

        verify(repository).findById(any(EducationLineId.class));
        verify(repository, never()).compensate(any(EducationLineId.class), any(SagaOutcome.class));
    }

    @Test
    void compensateEducationLine_WhenCompensateFails_ShouldReturnIdempotentTrue() {
        // Given
        PayloadCompensateCreate cmd = new PayloadCompensateCreate(
                EducationLineApplicationServiceTest.class,
                SagaOutcome.COMPENSATE);

        when(repository.findById(any(EducationLineId.class)))
                .thenReturn(Optional.of(aggregate(compensateId, "Math", true)));
        when(repository.compensate(any(EducationLineId.class), any(SagaOutcome.class)))
                .thenReturn(false);

        // When
        ResponseCompensated result = service.compensate(compensateId, cmd.getClass(), cmd.sagaState());

        // Then
        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertTrue(result.sagaState() == SagaOutcome.IDEMPOTENT);

        verify(repository).findById(any(EducationLineId.class));
        verify(repository).compensate(any(EducationLineId.class), any(SagaOutcome.class));
    }
    
    @Test
    void compensateEducationLineName_WhenEducationLineNameExistsAbdComensateSucceeds_ShouldReturnCompensated() {
        // Given
        PayloadCompensateCreate cmd = new PayloadCompensateCreate(
            EducationLineApplicationServiceTest.class,
            SagaOutcome.COMPENSATE
        );

        when(repository.findById(any(EducationLineId.class)))
            .thenReturn(Optional.of(aggregate(compensateId, "Math", true)));
        when(repository.compensateName(any(EducationLineId.class), any(SagaOutcome.class), any(EducationLineName.class))).thenReturn(true);

        // When
        ResponseCompensated result = service.compensateName(compensateId, cmd.getClass(), cmd.sagaState(),
                new UpdateEducationLineNameCmd("Science"));

        // Then
        assertEquals(SagaOutcome.COMPENSATED, result.sagaState());
        assertTrue(result.sagaState() == SagaOutcome.COMPENSATED);

        verify(repository).findById(any(EducationLineId.class));
        verify(repository).compensateName(any(EducationLineId.class), any(SagaOutcome.class), any(EducationLineName.class));
    }

    @Test
    void compensateEducationLineName_WhenEducationLineNameDoesNotExist_ShouldReturnIdempotentFalse() {
        // Given
        PayloadCompensateCreate cmd = new PayloadCompensateCreate(
            EducationLineApplicationServiceTest.class,
            SagaOutcome.COMPENSATE
        );

        when(repository.findById(any(EducationLineId.class)))
            .thenReturn(Optional.empty());

        // When
        ResponseCompensated result = service.compensateName(compensateId, cmd.getClass(), cmd.sagaState(),
                new UpdateEducationLineNameCmd("Science"));

        // Then
        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertFalse(result.sagaState() == SagaOutcome.COMPENSATED);

        verify(repository).findById(any(EducationLineId.class));
        verify(repository, never()).compensateName(any(EducationLineId.class), any(SagaOutcome.class), any(EducationLineName.class));
    }
    
    @Test
    void compensateEducationLineName_WhenCompensateFails_ShouldReturnIdempotentTrue() {
        // Given
        PayloadCompensateCreate cmd = new PayloadCompensateCreate(
                EducationLineApplicationServiceTest.class,
                SagaOutcome.COMPENSATE);

        when(repository.findById(any(EducationLineId.class)))
                .thenReturn(Optional.of(aggregate(compensateId, "Math", true)));
        when(repository.compensateName(any(EducationLineId.class), any(SagaOutcome.class), any(EducationLineName.class))).thenReturn(false);

        // When
        ResponseCompensated result = service.compensateName(compensateId, cmd.getClass(), cmd.sagaState(),
                new UpdateEducationLineNameCmd("Science"));

        // Then
        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertTrue(result.sagaState() == SagaOutcome.IDEMPOTENT);

        verify(repository).findById(any(EducationLineId.class));
        verify(repository).compensateName(any(EducationLineId.class), any(SagaOutcome.class), any(EducationLineName.class));
    }
    
    @Test
    void compensateEducationLineDuration_WhenEducationLineDurationExistsAndCompensateSucceeds_ShouldReturnCompensated() {
        // Given
        PayloadCompensateCreate cmd = new PayloadCompensateCreate(
            EducationLineApplicationServiceTest.class,
            SagaOutcome.COMPENSATE
        );

        when(repository.findById(any(EducationLineId.class)))
            .thenReturn(Optional.of(aggregate(compensateId, "Math", true)));
        when(repository.compensateDuration(any(EducationLineId.class), any(SagaOutcome.class), any(EducationLineDuration.class))).thenReturn(true);

        // When
        ResponseCompensated result = service.compensateDuration(compensateId, cmd.getClass(), cmd.sagaState(),
                new UpdateEducationLineDurationCmd(10, 10, 10));
        // Then
        assertEquals(SagaOutcome.COMPENSATED, result.sagaState());
        assertTrue(result.sagaState() == SagaOutcome.COMPENSATED);

        verify(repository).findById(any(EducationLineId.class));
        verify(repository).compensateDuration(any(EducationLineId.class), any(SagaOutcome.class), any(EducationLineDuration.class));
    }

    @Test
    void compensateEducationLineDuration_WhenEducationLineDurationDoesNotExist_ShouldReturnIdempotentFalse() {
        // Given
        PayloadCompensateCreate cmd = new PayloadCompensateCreate(
            EducationLineApplicationServiceTest.class,
            SagaOutcome.COMPENSATE
        );

        when(repository.findById(any(EducationLineId.class)))
            .thenReturn(Optional.empty());

        // When
        ResponseCompensated result = service.compensateDuration(compensateId, cmd.getClass(), cmd.sagaState(),
                new UpdateEducationLineDurationCmd(10, 10, 10));

        // Then
        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertFalse(result.sagaState() == SagaOutcome.COMPENSATED);

        verify(repository).findById(any(EducationLineId.class));
        verify(repository, never()).compensateDuration(any(EducationLineId.class), any(SagaOutcome.class), any(EducationLineDuration.class));
    }
    
    @Test
    void compensateEducationLineDuration_WhenCompensateFails_ShouldReturnIdempotentTrue() {
        // Given
        PayloadCompensateCreate cmd = new PayloadCompensateCreate(
                EducationLineApplicationServiceTest.class,
                SagaOutcome.COMPENSATE);

        when(repository.findById(any(EducationLineId.class)))
                .thenReturn(Optional.of(aggregate(compensateId, "Math", true)));
        when(repository.compensateDuration(any(EducationLineId.class), any(SagaOutcome.class),
                any(EducationLineDuration.class))).thenReturn(false);

        // When
        ResponseCompensated result = service.compensateDuration(compensateId, cmd.getClass(), cmd.sagaState(),
                new UpdateEducationLineDurationCmd(10, 10, 10));

        // Then
        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertTrue(result.sagaState() == SagaOutcome.IDEMPOTENT);

        verify(repository).findById(any(EducationLineId.class));
        verify(repository).compensateDuration(any(EducationLineId.class), any(SagaOutcome.class),
                any(EducationLineDuration.class));
    }
    
    @Test
    void compensateEducationLineActivate_WhenEducationLineActivateExistsAndCompensateSucceeds_ShouldReturnCompensated() {
        // Given
        PayloadCompensateCreate cmd = new PayloadCompensateCreate(
            EducationLineApplicationServiceTest.class,
            SagaOutcome.COMPENSATE
        );

        when(repository.findById(any(EducationLineId.class)))
            .thenReturn(Optional.of(aggregate(compensateId, "Math", false)));
        when(repository.compensateActivate(any(EducationLineId.class), any(SagaOutcome.class))).thenReturn(true);

        // When
        ResponseCompensated result = service.compensateActivate(compensateId, cmd.getClass(), cmd.sagaState());
        // Then
        assertEquals(SagaOutcome.COMPENSATED, result.sagaState());
        assertTrue(result.sagaState() == SagaOutcome.COMPENSATED);

        verify(repository).findById(any(EducationLineId.class));
        verify(repository).compensateActivate(any(EducationLineId.class), any(SagaOutcome.class));
    }

    @Test
    void compensateEducationLineActivate_WhenEducationLineActivateDoesNotExist_ShouldReturnIdempotentFalse() {
        // Given
        PayloadCompensateCreate cmd = new PayloadCompensateCreate(
            EducationLineApplicationServiceTest.class,
            SagaOutcome.COMPENSATE
        );

        when(repository.findById(any(EducationLineId.class)))
            .thenReturn(Optional.empty());

        // When
        ResponseCompensated result = service.compensateActivate(compensateId, cmd.getClass(), cmd.sagaState());

        // Then
        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertFalse(result.sagaState() == SagaOutcome.COMPENSATED);

        verify(repository).findById(any(EducationLineId.class));
        verify(repository, never()).compensateActivate(any(EducationLineId.class), any(SagaOutcome.class));
    }
    
    @Test
    void compensateEducationLineActivate_WhenCompensateFails_ShouldReturnIdempotentTrue() {
        // Given
        PayloadCompensateCreate cmd = new PayloadCompensateCreate(
                EducationLineApplicationServiceTest.class,
                SagaOutcome.COMPENSATE);

        when(repository.findById(any(EducationLineId.class)))
                .thenReturn(Optional.of(aggregate(compensateId, "Math", false)));
        when(repository.compensateActivate(any(EducationLineId.class), any(SagaOutcome.class))).thenReturn(false);

        // When
        ResponseCompensated result = service.compensateActivate(compensateId, cmd.getClass(), cmd.sagaState());

        // Then
        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertTrue(result.sagaState() == SagaOutcome.IDEMPOTENT);

        verify(repository).findById(any(EducationLineId.class));
        verify(repository).compensateActivate(any(EducationLineId.class), any(SagaOutcome.class));
    }
    
    @Test
    void compensateEducationLineDeactivate_WhenEducationLineDeactivateExistsAndCompensateSucceeds_ShouldReturnCompensated() {
        // Given
        PayloadCompensateCreate cmd = new PayloadCompensateCreate(
            EducationLineApplicationServiceTest.class,
            SagaOutcome.COMPENSATE
        );

        when(repository.findById(any(EducationLineId.class)))
            .thenReturn(Optional.of(aggregate(compensateId, "Math", true)));
        when(repository.compensateDeactivate(any(EducationLineId.class), any(SagaOutcome.class))).thenReturn(true);

        // When
        ResponseCompensated result = service.compensateDeactivate(compensateId, cmd.getClass(), cmd.sagaState());
        // Then
        assertEquals(SagaOutcome.COMPENSATED, result.sagaState());
        assertTrue(result.sagaState() == SagaOutcome.COMPENSATED);

        verify(repository).findById(any(EducationLineId.class));
        verify(repository).compensateDeactivate(any(EducationLineId.class), any(SagaOutcome.class));
    }

    @Test
    void compensateEducationLineDeactivate_WhenEducationLineDeactivateDoesNotExist_ShouldReturnIdempotentFalse() {
        // Given
        PayloadCompensateCreate cmd = new PayloadCompensateCreate(
            EducationLineApplicationServiceTest.class,
            SagaOutcome.COMPENSATE
        );

        when(repository.findById(any(EducationLineId.class)))
            .thenReturn(Optional.empty());

        // When
        ResponseCompensated result = service.compensateDeactivate(compensateId, cmd.getClass(), cmd.sagaState());

        // Then
        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertFalse(result.sagaState() == SagaOutcome.COMPENSATED);

        verify(repository).findById(any(EducationLineId.class));
        verify(repository, never()).compensateDeactivate(any(EducationLineId.class), any(SagaOutcome.class));
    }
    
    @Test
    void compensateEducationLineDeactivate_WhenCompensateFails_ShouldReturnIdempotentTrue() {
        // Given
        PayloadCompensateCreate cmd = new PayloadCompensateCreate(
                EducationLineApplicationServiceTest.class,
                SagaOutcome.COMPENSATE);

        when(repository.findById(any(EducationLineId.class)))
                .thenReturn(Optional.of(aggregate(compensateId, "Math", true)));
        when(repository.compensateDeactivate(any(EducationLineId.class), any(SagaOutcome.class))).thenReturn(false);

        // When
        ResponseCompensated result = service.compensateDeactivate(compensateId, cmd.getClass(), cmd.sagaState());

        // Then
        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertTrue(result.sagaState() == SagaOutcome.IDEMPOTENT);

        verify(repository).findById(any(EducationLineId.class));
        verify(repository).compensateDeactivate(any(EducationLineId.class), any(SagaOutcome.class));
    }
}