package local.sop.sopinfo.workhour.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public class WorkScheduleTimeTest {

    @Test
    void happypath_24h_time_will_succeed() {
        WorkScheduleTime time1 = new WorkScheduleTime("00:00");
        WorkScheduleTime time2 = new WorkScheduleTime("23:59");
        assertNotNull(time1);
        assertNotNull(time2);
    }
    @Test
    void happypath_of_24h_will_succeed() {
        WorkScheduleTime time1 = WorkScheduleTime.of("00:00");
        WorkScheduleTime time2 = WorkScheduleTime.of("23:59");
        assertNotNull(time1);
        assertNotNull(time2);
    }

     @Test
     void happyPath_toString_will_print_beautified() {
        WorkScheduleTime time1 = WorkScheduleTime.of("00:00");
        assertEquals("WorkScheduleTime{time: 00:00}", time1.toString());
     }

    @Test
    void unhappypath_am_pm_will_fail() {
        assertThrows(ValidationException.class, () -> new WorkScheduleTime("7pm"));
        assertThrows(ValidationException.class, () -> new WorkScheduleTime("9am"));
    }
    @Test
    void unhappypath_null_empty_blank_will_fail() {
        assertThrows(ValidationException.class, () -> new WorkScheduleTime(null));
        assertThrows(ValidationException.class, () -> new WorkScheduleTime(""));
        assertThrows(ValidationException.class, () -> new WorkScheduleTime("    "));
    }

    @Test
    void unhappypath_illegal_string_will_fail() {
        assertThrows(ValidationException.class, () -> new WorkScheduleTime("test"));

    }
    @Test
    void unhappypath_fail_will_translate_to_danish() {
        var ex =  assertThrows(ValidationException.class, () -> new WorkScheduleTime("test"));
        assertEquals("time.invalid", ex.getMessage());
    }
}
