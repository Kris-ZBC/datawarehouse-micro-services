package local.sop.sopinfo.educationline.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.educationline.domain.model.EducationLine;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineDuration;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineId;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineName;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationRef;

import local.sop.common.libs.sharedkernel.exceptions.ConflictException;

class EducationLineRepositoryAdapterTest {

    private EducationLineSpringDataRepository jpa;
    private EducationLineRepositoryAdapter adapter;

    private static EducationLineEntity entity(UUID id, String name, boolean active) {
        return new EducationLineEntity(
                id,
                name,
                1,
                2,
                3,
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                active);
    }

    @BeforeEach
    void setup() {
        jpa = Mockito.mock(EducationLineSpringDataRepository.class);
        adapter = new EducationLineRepositoryAdapter(jpa);
    }

    @Test
    void save_find_and_update_methods_map_correctly() {
        UUID id = UUID.randomUUID();
        EducationLineEntity savedEntity = entity(id, "Math", true);

        when(jpa.save(any(EducationLineEntity.class))).thenReturn(savedEntity);
        when(jpa.findAll()).thenReturn(List.of(savedEntity));
        when(jpa.findBySearchParams(any(), any())).thenReturn(List.of(savedEntity));
        when(jpa.findById(id)).thenReturn(Optional.of(savedEntity));
        when(jpa.existsById(id)).thenReturn(true);

        EducationLine saved = adapter.save(new EducationLineJpaMapper().toDomain(savedEntity));
        List<EducationLine> all = adapter.findAll();
        List<EducationLine> filtered = adapter.findBySearchParams(id, "Mat");
        Optional<EducationLine> byId = adapter.findById(new EducationLineId(id));
        Optional<EducationLine> byName = adapter.updateEducationLineName(new EducationLineId(id), new EducationLineName("Physics"));
        Optional<EducationLine> byDuration = adapter.updateEducationLineDuration(new EducationLineId(id), new EducationLineDuration(1, 0, 1));

        assertNotNull(saved);
        assertEquals(1, all.size());
        assertEquals(1, filtered.size());
        assertTrue(byId.isPresent());
        assertTrue(byName.isPresent());
        assertTrue(byDuration.isPresent());

        verify(jpa).save(any(EducationLineEntity.class));
        verify(jpa).findAll();
        verify(jpa).findBySearchParams(id, "Mat");
        verify(jpa, Mockito.times(3)).findById(id);
        verify(jpa).update(id, "Physics", null, null, null);
        verify(jpa).update(id, null, 1, 0, 1);
    }

	@Test
	void findByEducation_maps_correctly() {

		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		EducationRef eduacationRef = new EducationRef(id);
		
		EducationLineEntity entity = entity(UUID.randomUUID(), "Math", true);

		when(jpa.findByEducationRef(id)).thenReturn(List.of(entity));

		List<EducationLine> result = adapter.findByEducationRef(eduacationRef);
		assertFalse(result.isEmpty());
		verify(jpa).findByEducationRef(id);
	}

    @Test
    void update_methods_return_empty_when_id_missing() {
        UUID id = UUID.randomUUID();
        when(jpa.existsById(id)).thenReturn(false);

        Optional<EducationLine> byName = adapter.updateEducationLineName(new EducationLineId(id), new EducationLineName("Physics"));
        Optional<EducationLine> byDuration = adapter.updateEducationLineDuration(new EducationLineId(id), new EducationLineDuration(1, 0, 1));

        assertTrue(byName.isEmpty());
        assertTrue(byDuration.isEmpty());
    }

    @Test
    void activate_and_deactivate_delegate_to_jpa() {
        UUID id = UUID.randomUUID();
        EducationLineId vo = new EducationLineId(id);
        EducationLineEntity activeEntity = entity(id, "Math", true);
        EducationLineEntity inactiveEntity = entity(id, "Math", false);

        when(jpa.existsById(id)).thenReturn(true);
        when(jpa.findById(id))
                .thenReturn(Optional.of(inactiveEntity))
                .thenReturn(Optional.of(activeEntity));

        Optional<EducationLine> deactivated = adapter.deactivate(vo);
        Optional<EducationLine> activated = adapter.activate(vo);

        assertTrue(deactivated.isPresent());
        assertTrue(activated.isPresent());
        verify(jpa).deactivate(id);
        verify(jpa).activate(id);
    }

    @Test
    void compensate_when_saga_state_invalid_throws_conflict() {
        UUID id = UUID.randomUUID();

        assertThrows(ConflictException.class, () ->
            adapter.compensate(new EducationLineId(id), SagaOutcome.SUCCEEDED)
        );
        verify(jpa, never()).findById(id);
        verify(jpa, never()).delete(id);
    }

    @Test
    void compensate_when_line_not_found_returns_false_and_skips_delete() {
        UUID id = UUID.randomUUID();
        when(jpa.findById(id)).thenReturn(Optional.empty());

        Boolean result = adapter.compensate(new EducationLineId(id), SagaOutcome.COMPENSATE);

        assertFalse(result);
        verify(jpa).findById(id);
        verify(jpa, never()).delete(id);
    }

    @Test
    void compensate_when_delete_count_is_one_returns_true() {
        UUID id = UUID.randomUUID();
        when(jpa.findById(id)).thenReturn(Optional.of(entity(id, "Math", true)));
        when(jpa.delete(id)).thenReturn(1);

        Boolean result = adapter.compensate(new EducationLineId(id), SagaOutcome.COMPENSATE);

        assertTrue(result);
        verify(jpa).findById(id);
        verify(jpa).delete(id);
    }

    @Test
    void compensate_when_delete_count_is_zero_returns_false() {
        UUID id = UUID.randomUUID();
        when(jpa.findById(id)).thenReturn(Optional.of(entity(id, "Math", true)));
        when(jpa.delete(id)).thenReturn(0);

        Boolean result = adapter.compensate(new EducationLineId(id), SagaOutcome.COMPENSATE);

        assertFalse(result);
        verify(jpa).findById(id);
        verify(jpa).delete(id);
    }

    @Test
    void compensateActivate_when_saga_state_invalid_throws_conflict() {
        UUID id = UUID.randomUUID();

        assertThrows(ConflictException.class, () ->
            adapter.compensateActivate(new EducationLineId(id), SagaOutcome.SUCCEEDED)
        );
        verify(jpa, never()).compensateActivate(any());
    }

    @Test
    void compensateActivate_when_line_not_found_returns_false() {
        UUID id = UUID.randomUUID();
        when(jpa.findById(id)).thenReturn(Optional.empty());

        Boolean result = adapter.compensateActivate(new EducationLineId(id), SagaOutcome.COMPENSATE);

        assertFalse(result);
        verify(jpa, never()).compensateActivate(id);
    }

    @Test
    void compensateActivate_delegates_to_jpa() {
        UUID id = UUID.randomUUID();
        when(jpa.findById(id)).thenReturn(Optional.of(entity(id, "Math", true)));
        when(jpa.compensateActivate(id)).thenReturn(true);

        Boolean result = adapter.compensateActivate(new EducationLineId(id), SagaOutcome.COMPENSATE);

        assertTrue(result);
        verify(jpa).compensateActivate(id);
    }

    @Test
    void compensateDeactivate_when_saga_state_invalid_throws_conflict() {
        UUID id = UUID.randomUUID();

        assertThrows(ConflictException.class, () ->
            adapter.compensateDeactivate(new EducationLineId(id), SagaOutcome.SUCCEEDED)
        );
        verify(jpa, never()).compensateDeactivate(any());
    }

    @Test
    void compensateDeactivate_when_line_not_found_returns_false() {
        UUID id = UUID.randomUUID();
        when(jpa.findById(id)).thenReturn(Optional.empty());

        Boolean result = adapter.compensateDeactivate(new EducationLineId(id), SagaOutcome.COMPENSATE);

        assertFalse(result);
        verify(jpa, never()).compensateDeactivate(id);
    }

    @Test
    void compensateDeactivate_delegates_to_jpa() {
        UUID id = UUID.randomUUID();
        when(jpa.findById(id)).thenReturn(Optional.of(entity(id, "Math", true)));
        when(jpa.compensateDeactivate(id)).thenReturn(true);

        Boolean result = adapter.compensateDeactivate(new EducationLineId(id), SagaOutcome.COMPENSATE);

        assertTrue(result);
        verify(jpa).compensateDeactivate(id);
    }

    @Test
    void compensateName_when_saga_state_invalid_throws_conflict() {
        UUID id = UUID.randomUUID();
        EducationLineName name = new EducationLineName("Physics");

        assertThrows(ConflictException.class, () ->
            adapter.compensateName(new EducationLineId(id), SagaOutcome.SUCCEEDED, name)
        );
        verify(jpa, never()).compensateName(any(), any());
    }

    @Test
    void compensateName_when_line_not_found_returns_false() {
        UUID id = UUID.randomUUID();
        EducationLineName name = new EducationLineName("Physics");
        when(jpa.findById(id)).thenReturn(Optional.empty());

        Boolean result = adapter.compensateName(new EducationLineId(id), SagaOutcome.COMPENSATE, name);

        assertFalse(result);
        verify(jpa, never()).compensateName(id, name.value());
    }

    @Test
    void compensateName_delegates_to_jpa() {
        UUID id = UUID.randomUUID();
        EducationLineName name = new EducationLineName("Physics");
        when(jpa.findById(id)).thenReturn(Optional.of(entity(id, "Math", true)));
        when(jpa.compensateName(id, name.value())).thenReturn(true);

        Boolean result = adapter.compensateName(new EducationLineId(id), SagaOutcome.COMPENSATE, name);

        assertTrue(result);
        verify(jpa).compensateName(id, name.value());
    }

    @Test
    void compensateDuration_when_saga_state_invalid_throws_conflict() {
        UUID id = UUID.randomUUID();
        EducationLineDuration duration = new EducationLineDuration(1, 2, 3);

        assertThrows(ConflictException.class, () ->
            adapter.compensateDuration(new EducationLineId(id), SagaOutcome.SUCCEEDED, duration)
        );
        verify(jpa, never()).compensateDuration(any(), any(), any(), any());
    }

    @Test
    void compensateDuration_when_line_not_found_returns_false() {
        UUID id = UUID.randomUUID();
        EducationLineDuration duration = new EducationLineDuration(1, 2, 3);
        when(jpa.findById(id)).thenReturn(Optional.empty());

        Boolean result = adapter.compensateDuration(new EducationLineId(id), SagaOutcome.COMPENSATE, duration);

        assertFalse(result);
        verify(jpa, never()).compensateDuration(id, 1, 2, 3);
    }

    @Test
    void compensateDuration_delegates_to_jpa() {
        UUID id = UUID.randomUUID();
        EducationLineDuration duration = new EducationLineDuration(1, 2, 3);
        when(jpa.findById(id)).thenReturn(Optional.of(entity(id, "Math", true)));
        when(jpa.compensateDuration(id, 1, 2, 3)).thenReturn(true);

        Boolean result = adapter.compensateDuration(new EducationLineId(id), SagaOutcome.COMPENSATE, duration);

        assertTrue(result);
        verify(jpa).compensateDuration(id, 1, 2, 3);
    }
}
