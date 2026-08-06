package local.sop.datawarehouse.workhour.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.workhour.domain.model.valueobjects.WorkHourId;

public class WorkHourTest {

    private static final WorkHourId VALID_ID = WorkHourId.newId();
    private static final LocalTime START_TIME = LocalTime.of(7, 45);
    private static final LocalTime END_TIME = LocalTime.of(8, 15);
    private static final LocalTime NEW_START_TIME = LocalTime.of(15, 45);
    private static final LocalTime NEW_END_TIME = LocalTime.of(15, 15);

    // ── Builder happy path ─────────────────────────────────────────────────────

    @Test
    void builder_shouldCreateAggregate_whenAllFieldsAreValid() {
        WorkHour aggregate = WorkHour.builder()
            .id(VALID_ID)
            .startTime(START_TIME)
            .endTime(END_TIME)
            .build();

        assertEquals(VALID_ID, aggregate.getId());
        assertEquals(START_TIME, aggregate.getStartTime());
        assertEquals(END_TIME, aggregate.getEndTime());
    }

    // ── Builder unhappy path ─────────────────────────────────────────────────────

    @Test
    void builder_shouldThrowValidationException_whenIdIsNull() {
        ValidationException ex = assertThrows(ValidationException.class, () -> WorkHour.builder()
            .startTime(START_TIME)
            .endTime(END_TIME)
            .build());

        assertEquals("workhour.id.required", ex.getMessage());
    }

    @Test
    void builder_shouldThrowValidationException_whenStartTimeIsNull() {
        ValidationException ex = assertThrows(ValidationException.class,() -> WorkHour.builder()
            .id(VALID_ID)
            .endTime(END_TIME)
            .build());

        assertEquals("workhour.starttime.required", ex.getMessage());
    }

    @Test
    void builder_shouldThrowValidationException_whenEndTimeIsNull() {
        ValidationException ex = assertThrows(ValidationException.class, () -> WorkHour.builder()
            .id(VALID_ID)
            .startTime(START_TIME)
            .build());

        assertEquals("workhour.endtime.required", ex.getMessage());
    }

    @Test
    void builder_shouldThrowValidationException_whenStartAndEndTimeAreEqual() {
        ValidationException ex = assertThrows(ValidationException.class, () -> WorkHour.builder()
            .id(VALID_ID)
            .startTime(START_TIME)
            .endTime(START_TIME)
            .build());

        assertEquals("workhour.timeframe.invalid", ex.getMessage());
    }

    @Test
    void builder_shouldThrowValidationException_whenEndTimeIsBeforeStartTime() {
        ValidationException ex = assertThrows(ValidationException.class, () -> WorkHour.builder()
            .id(VALID_ID)
            .startTime(END_TIME)
            .endTime(START_TIME)
            .build());

        assertEquals("workhour.timeframe.invalid", ex.getMessage());
    }

// ── withStartTime ──────────────────────────────────────────────────────────

    @Test
    void withStartTime_shouldReturnNewInstance_withUpdatedStartTime() {
        WorkHour original = validWorkHour();

        WorkHour updated = original.withStartTime(NEW_START_TIME);

        assertEquals(NEW_START_TIME, updated.getStartTime());
    }

    @Test
    void withStartTime_shouldReturnNewInstance_notSameReference() {
        WorkHour original = validWorkHour();

        WorkHour updated = original.withStartTime(NEW_START_TIME);

        assertNotSame(original, updated);
    }

    @Test
    void withStartTime_shouldPreserveId_andEndTime() {
        WorkHour original = validWorkHour();

        WorkHour updated = original.withStartTime(NEW_START_TIME);

        assertEquals(original.getId(), updated.getId());
        assertEquals(original.getEndTime(), updated.getEndTime());
    }

// ── withEndTime ────────────────────────────────────────────────────────────

    @Test
    void withEndTime_shouldReturnNewInstance_withUpdatedEndTime() {
        WorkHour original = validWorkHour();

        WorkHour updated = original.withEndTime(NEW_END_TIME);

        assertEquals(NEW_END_TIME, updated.getEndTime());
    }

    @Test
    void withEndTime_shouldReturnNewInstance_notSameReference() {
        WorkHour original = validWorkHour();

        WorkHour updated = original.withEndTime(NEW_END_TIME);

        assertNotSame(original, updated);
    }

    @Test
    void withEndTime_shouldPreserveId_andStartTime() {
        WorkHour original = validWorkHour();

        WorkHour updated = original.withEndTime(NEW_END_TIME);

        assertEquals(original.getId(), updated.getId());
        assertEquals(original.getStartTime(), updated.getStartTime());
    }

// ── Immutability ───────────────────────────────────────────────────────────

    @Test
    void withStartTime_shouldNotMutateOriginal() {
        WorkHour original = validWorkHour();

        original.withStartTime(NEW_START_TIME);

        assertEquals(START_TIME, original.getStartTime());
    }

    @Test
    void withEndTime_shouldNotMutateOriginal() {
        WorkHour original = validWorkHour();

        original.withEndTime(NEW_END_TIME);

        assertEquals(END_TIME, original.getEndTime());
    }

// ── Getters ────────────────────────────────────────────────────────────────

    @Test
    void getId_shouldReturnCorrectValue() {
        WorkHour aggregate = validWorkHour();

        assertEquals(VALID_ID, aggregate.getId());
    }

    @Test
    void getStartTime_shouldReturnCorrectValue() {
        WorkHour aggregate = validWorkHour();

        assertEquals(START_TIME, aggregate.getStartTime());
    }

    @Test
    void getEndTime_shouldReturnCorrectValue() {
        WorkHour aggregate = validWorkHour();

        assertEquals(END_TIME, aggregate.getEndTime());
    }

// ── Helper ─────────────────────────────────────────────────────────────────

    private WorkHour validWorkHour() {
        return WorkHour.builder()
            .id(VALID_ID)
            .startTime(START_TIME)
            .endTime(END_TIME)
            .build();
    }

}
