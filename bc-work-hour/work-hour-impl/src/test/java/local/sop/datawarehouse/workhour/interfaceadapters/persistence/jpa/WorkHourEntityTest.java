package local.sop.datawarehouse.workhour.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

class WorkHourEntityTest {

// ── Builder happy path ─────────────────────────────────────────────────────

    @Test
    void builder_createsEntity_andGettersWork() {

        UUID id = UUID.randomUUID();

        WorkHourEntity entity = WorkHourEntity.builder()
            .id(id)
            .startTime(LocalTime.of(7, 45))
            .endTime(LocalTime.of(15, 45))
            .build();

        assertEquals(id, entity.getId());
        assertEquals(LocalTime.of(7, 45), entity.getStartTime());
        assertEquals(LocalTime.of(15, 45), entity.getEndTime());
    }

// ── Builder validation ─────────────────────────────────────────────────────

    @Test
    void builder_throws_whenIdMissing() {

        ValidationException ex = assertThrows(ValidationException.class, () -> WorkHourEntity.builder()
            .startTime(LocalTime.of(7, 45))
            .endTime(LocalTime.of(15, 45))
            .build());

        assertEquals("workhour.id.required", ex.getMessage());
    }

    @Test
    void builder_throws_whenStartTimeMissing() {

        ValidationException ex = assertThrows(ValidationException.class, () -> WorkHourEntity.builder()
            .id(UUID.randomUUID())
            .endTime(LocalTime.of(15, 45))
            .build());

        assertEquals("workhour.starttime.required", ex.getMessage());
    }

    @Test
    void builder_throws_whenEndTimeMissing() {

        ValidationException ex = assertThrows(ValidationException.class, () -> WorkHourEntity.builder()
            .id(UUID.randomUUID())
            .startTime(LocalTime.of(7, 45))
            .build());

        assertEquals("workhour.endtime.required", ex.getMessage());
    }

    @Test
    void builder_throws_whenEndTimeEqualsStartTime() {

        ValidationException ex = assertThrows(ValidationException.class, () -> WorkHourEntity.builder()
            .id(UUID.randomUUID())
            .startTime(LocalTime.of(7, 45))
            .endTime(LocalTime.of(7, 45))
            .build());

        assertEquals("workhour.timeframe.invalid", ex.getMessage());
    }

    @Test
    void builder_throws_whenEndTimeBeforeStartTime() {

        ValidationException ex = assertThrows(ValidationException.class, () -> WorkHourEntity.builder()
            .id(UUID.randomUUID())
            .startTime(LocalTime.of(15, 45))
            .endTime(LocalTime.of(7, 45))
            .build());

        assertEquals("workhour.timeframe.invalid", ex.getMessage());
    }

    @Test
    void builder_id_shouldThrow_whenIdIsNull() {

        ValidationException ex = assertThrows(ValidationException.class,() -> WorkHourEntity.builder().id(null));

        assertEquals("workhour.id.required", ex.getMessage());
    }

    @Test
    void builder_startTime_shouldThrow_whenStartTimeIsNull() {

        ValidationException ex = assertThrows(ValidationException.class,() -> WorkHourEntity.builder().startTime(null));

        assertEquals("workhour.starttime.required", ex.getMessage());
    }

    @Test
    void builder_endTime_shouldThrow_whenEndTimeIsNull() {

        ValidationException ex = assertThrows(ValidationException.class, () -> WorkHourEntity.builder().endTime(null));

        assertEquals("workhour.endtime.required", ex.getMessage());
    }

// ── withStartTime ──────────────────────────────────────────────────────────

    @Test
    void withStartTime_updatesField() {

        WorkHourEntity entity = validEntity();

        entity.withStartTime(LocalTime.of(8, 0));

        assertEquals(LocalTime.of(8, 0), entity.getStartTime());
    }

// ── withEndTime ────────────────────────────────────────────────────────────

    @Test
    void withEndTime_updatesField() {

        WorkHourEntity entity = validEntity();

        entity.withEndTime(LocalTime.of(17, 0));

        assertEquals(LocalTime.of(17, 0), entity.getEndTime());
    }

// ── Helper ─────────────────────────────────────────────────────────────────

    private WorkHourEntity validEntity() {
        return WorkHourEntity.builder()
            .id(UUID.randomUUID())
            .startTime(LocalTime.of(7, 45))
            .endTime(LocalTime.of(15, 45))
            .build();
    }

}
