package local.sop.sopinfo.workhour.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.enums.WeekDay;
import local.sop.common.libs.sharedkernel.valueobjects.utils.UUIDUtil;

class WorkHourScheduleEntityTest {

    private WorkHourScheduleEntity baseEntity(UUID id, UUID sopRef) {
        return WorkHourScheduleEntity.builder()
            .id(id)
            .startTime("07:00")
            .endTime("16:00")
            .weekDay(WeekDay.MONDAY)
            .sopRef(sopRef)
            .build();
    }

    @Test
    void withStartTime_shouldReturnNewEntityWithUpdatedStartTime() {
        UUID id = UUIDUtil.newUuid();
        UUID sopRef = UUIDUtil.newUuid();
        WorkHourScheduleEntity original = baseEntity(id, sopRef);

        WorkHourScheduleEntity updated = original.withStartTime("08:30");

        assertNotSame(original, updated);
        assertEquals(id, updated.getId());
        assertEquals("08:30", updated.getStartTime());
        assertEquals(original.getEndTime(), updated.getEndTime());
        assertEquals(original.getWeekDay(), updated.getWeekDay());
        assertEquals(sopRef, updated.getSopRef());
    }

    @Test
    void withEndTime_shouldReturnNewEntityWithUpdatedEndTime() {
        UUID id = UUIDUtil.newUuid();
        UUID sopRef = UUIDUtil.newUuid();
        WorkHourScheduleEntity original = baseEntity(id, sopRef);

        WorkHourScheduleEntity updated = original.withEndTime("17:30");

        assertNotSame(original, updated);
        assertEquals(id, updated.getId());
        assertEquals(original.getStartTime(), updated.getStartTime());
        assertEquals("17:30", updated.getEndTime());
        assertEquals(original.getWeekDay(), updated.getWeekDay());
        assertEquals(sopRef, updated.getSopRef());
    }

    @Test
    void withWeekDay_shouldReturnNewEntityWithUpdatedWeekDay() {
        UUID id = UUIDUtil.newUuid();
        UUID sopRef = UUIDUtil.newUuid();
        WorkHourScheduleEntity original = baseEntity(id, sopRef);

        WorkHourScheduleEntity updated = original.withWeekDay(WeekDay.FRIDAY);

        assertNotSame(original, updated);
        assertEquals(id, updated.getId());
        assertEquals(original.getStartTime(), updated.getStartTime());
        assertEquals(original.getEndTime(), updated.getEndTime());
        assertEquals(WeekDay.FRIDAY, updated.getWeekDay());
        assertEquals(sopRef, updated.getSopRef());
    }

    @Test
    void withSopRef_shouldReturnNewEntityWithUpdatedSopRef() {
        UUID id = UUIDUtil.newUuid();
        UUID sopRef = UUIDUtil.newUuid();
        UUID updatedSopRef = UUIDUtil.newUuid();
        WorkHourScheduleEntity original = baseEntity(id, sopRef);

        WorkHourScheduleEntity updated = original.withSopRef(updatedSopRef);

        assertNotSame(original, updated);
        assertEquals(id, updated.getId());
        assertEquals(original.getStartTime(), updated.getStartTime());
        assertEquals(original.getEndTime(), updated.getEndTime());
        assertEquals(original.getWeekDay(), updated.getWeekDay());
        assertEquals(updatedSopRef, updated.getSopRef());
    }
}
