package local.sop.sopinfo.workhour.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.sopinfo.workhour.application.api.dto.CreateWorkHourCmd;
import local.sop.sopinfo.workhour.application.api.dto.CreatedWorkHourResult;
import local.sop.sopinfo.workhour.application.api.dto.UpdateWorkHourCmd;
import local.sop.sopinfo.workhour.application.api.dto.WorkHourResponse;
import local.sop.sopinfo.workhour.domain.model.WorkHour;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkHourId;
import local.sop.sopinfo.workhour.domain.ports.out.WorkHourRepositoryPort;
import local.sop.sopinfo.workhour.domain.service.WorkHourDomain;

@ExtendWith(MockitoExtension.class)
class WorkHourApplicationServiceTest {

    @Mock
    private WorkHourRepositoryPort repository;

    @Mock
    private WorkHourDomain domain;

    @InjectMocks
    private WorkHourApplicationService service;

    private static final UUID VALID_ID = UUID.randomUUID();

    // ── create ─────────────────────────────────────────────────────────────────

    @Test
    void create_shouldSaveAggregateAndReturnResult() {
        WorkHour workHour = WorkHour.builder()
            .id(WorkHourId.of(VALID_ID))
            .startTime(LocalTime.of(7, 45))
            .endTime(LocalTime.of(15, 45))
            .build();

        when(domain.create(LocalTime.of(7, 45), LocalTime.of(15, 45))) .thenReturn(workHour);
        when(repository.save(any())) .thenReturn(workHour);

        CreatedWorkHourResult result = service.create(new CreateWorkHourCmd(LocalTime.of(7, 45), LocalTime.of(15, 45)));

        assertEquals(VALID_ID, result.id());
        verify(domain).create(LocalTime.of(7, 45), LocalTime.of(15, 45));
        verify(repository).save(any());
    }

    // ── update ─────────────────────────────────────────────────────────────────

    @Test
    void update_shouldUpdateAggregateAndReturnResult() {
        WorkHour updated = WorkHour.builder()
            .id(WorkHourId.of(VALID_ID))
            .startTime(LocalTime.of(8, 00))
            .endTime(LocalTime.of(15, 15))
            .build();

            when(domain.update(WorkHourId.of(VALID_ID), LocalTime.of(8, 00), LocalTime.of(15, 15))).thenReturn(updated);
            when(repository.update(any())).thenReturn(updated);

            WorkHourResponse result = service.update(new UpdateWorkHourCmd(VALID_ID, LocalTime.of(8, 00), LocalTime.of(15, 15)));

            assertEquals(VALID_ID, result.id());
            assertEquals(LocalTime.of(8, 00), result.startTime());
            assertEquals(LocalTime.of(15, 15), result.endTime());

            verify(domain).update(WorkHourId.of(VALID_ID), LocalTime.of(8, 00), LocalTime.of(15, 15));
            verify(repository).update(any());
    }

    // ── findById ────────────────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnResponse_whenEntityExists() {
        WorkHour workhour = WorkHour.builder()
            .id(WorkHourId.of(VALID_ID))
            .startTime(LocalTime.of(7, 45))
            .endTime(LocalTime.of(15, 45))
            .build();

        when(repository.findById(WorkHourId.of(VALID_ID))).thenReturn(Optional.of(workhour));

        Optional<WorkHourResponse> result = service.findById(VALID_ID);

        assertTrue(result.isPresent());
        assertEquals(VALID_ID, result.get().id());
        assertEquals(LocalTime.of(7, 45), result.get().startTime());
        assertEquals(LocalTime.of(15, 45), result.get().endTime());
    }

    @Test
    void findById_shouldReturnEmpty_whenEntityDoesNotExist() {
        when(repository.findById(WorkHourId.of(VALID_ID))).thenReturn(Optional.empty());

        Optional<WorkHourResponse> result = service.findById(VALID_ID);

        assertFalse(result.isPresent());
    }

    // ── getAll ───────────────────────────────────────────────────────────────

    @Test
    void getAll_shouldReturnAllResults() {
        WorkHour workhour1 = WorkHour.builder()
            .id(WorkHourId.of(UUID.randomUUID()))
            .startTime(LocalTime.of(7, 45))
            .endTime(LocalTime.of(15, 45))
            .build();

        WorkHour workhour2 = WorkHour.builder()
            .id(WorkHourId.of(UUID.randomUUID()))
            .startTime(LocalTime.of(8, 00))
            .endTime(LocalTime.of(16, 00))
            .build();

        when(repository.getAll()).thenReturn(List.of(workhour1, workhour2));

        List<WorkHourResponse> result = service.getAll();

        assertEquals(2, result.size());
        assertEquals(workhour1.getId().value(), result.get(0).id());
        assertEquals(workhour2.getId().value(), result.get(1).id());
    }
}
