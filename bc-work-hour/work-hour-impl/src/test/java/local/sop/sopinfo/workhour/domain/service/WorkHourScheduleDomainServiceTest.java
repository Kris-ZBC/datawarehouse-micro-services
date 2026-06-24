package local.sop.sopinfo.workhour.domain.service;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.sharedkernel.enums.WeekDay;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.sharedkernel.valueobjects.utils.UUIDUtil;
import local.sop.sopinfo.workhour.domain.model.WorkHourSchedule;
import local.sop.sopinfo.workhour.domain.model.valueobjects.SopRef;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkScheduleId;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkScheduleTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WorkHourScheduleDomainServiceTest {
    private final WorkHourScheduleDomainService service = new WorkHourScheduleDomainService();

    @Test
    void unhappyPath_createInvalid_ShouldThrowException() {
        var ex = assertThrows(ValidationException.class, () -> WorkHourSchedule.builder().id(WorkScheduleId.newId())
            .startTime(WorkScheduleTime.of("invalid")).endTime(WorkScheduleTime.of("16:00")).weekDay(WeekDay.FRIDAY).sopRef(SopRef.of(UUIDUtil.newUuid())).build());
        assertEquals("time.invalid", ex.getMessage());
    }

    @Test
    void happyPath_delete_shouldReturnWorkHourScheduleWithSameKeyAndDefaults() {
        var id = UUIDUtil.newUuid();

        WorkHourSchedule result = service.delete(id);

        assertNotNull(result);
        assertEquals(id, result.getId().value());
        assertEquals("00:00", result.getStartTime().time());
        assertEquals("00:00", result.getEndTime().time());
        assertEquals(WeekDay.FRIDAY, result.getWeekDay());
        assertNotNull(result.getSopRef());
        assertNotNull(result.getSopRef().value());
    }

    @Test
    void unhappyPath_delete_withNullId_shouldThrowException() {
        assertThrows(ValidationException.class, () -> service.delete(null));
    }
}
