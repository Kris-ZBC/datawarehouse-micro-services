package local.sop.sopinfo.workhour.domain.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;


import local.sop.common.libs.sharedkernel.enums.WeekDay;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.valueobjects.utils.UUIDUtil;
import local.sop.sopinfo.workhour.domain.model.valueobjects.SopRef;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkScheduleId;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkScheduleTime;

public class WorkHourScheduleTest {
    
    /* Blackbox tests */
    
    @Test
    public void happyPath_builder_no_id_shouldReturnNewWorkHourSchedule() {
        WorkHourSchedule schedule = 
            WorkHourSchedule.builder()
            .startTime(WorkScheduleTime.of("08:00"))
            .endTime(WorkScheduleTime.of("17:00"))
            .weekDay(WeekDay.MONDAY)
            .sopRef(new SopRef(UUID.randomUUID())).build();
        assertNotNull(schedule);
        assertNotNull(schedule.getId());
        assertTrue(schedule.getId().value() instanceof UUID);   
        assertEquals("08:00", schedule.getStartTime().time());
        assertEquals("17:00", schedule.getEndTime().time());
        assertEquals(WeekDay.MONDAY, schedule.getWeekDay());
        assertTrue(schedule.getSopRef().value() instanceof UUID);
    
    } 

    @Test
    public void happyPath_builder_with_id_shouldReturnNewWorkHourSchedule() {
        UUID id = UUIDUtil.newUuid();
        WorkHourSchedule schedule = 
            WorkHourSchedule.builder()
            .id(WorkScheduleId.of(id))
            .startTime(WorkScheduleTime.of("08:00"))
            .endTime(WorkScheduleTime.of("17:00"))
            .weekDay(WeekDay.MONDAY)
            .sopRef(new SopRef(UUID.randomUUID())).build();
        assertNotNull(schedule);
        assertEquals(id, schedule.getId().value());   
        assertEquals("08:00", schedule.getStartTime().time());
        assertEquals("17:00", schedule.getEndTime().time());
        assertEquals(WeekDay.MONDAY, schedule.getWeekDay());
        assertTrue(schedule.getSopRef().value() instanceof UUID);
    
    } 

    @Test
    public void happyPath_withers_shouldReturnNewWorkHourSchedule() {
        var ex = assertThrows(ValidationException.class, () -> WorkHourSchedule.builder().build());
        assertEquals("time.invalid", ex.getMessage());
        ex = assertThrows(ValidationException.class, 
            () -> WorkHourSchedule.builder()
            .startTime(WorkScheduleTime.of("08:00"))
            .endTime(WorkScheduleTime.of("18:00"))
            .build());
        assertEquals("weekday.invalid", ex.getMessage());
        ex = assertThrows(ValidationException.class, 
            () -> WorkHourSchedule.builder()
            .startTime(WorkScheduleTime.of("08:00"))
            .endTime(WorkScheduleTime.of("18:00"))
            .weekDay(WeekDay.MONDAY)
            .build());
        assertEquals("sopref.invalid", ex.getMessage());  
        WorkHourSchedule schedule = WorkHourSchedule.builder()
            .startTime(WorkScheduleTime.of("08:00"))
            .endTime(WorkScheduleTime.of("18:00"))
            .weekDay(WeekDay.MONDAY)
            .sopRef(new SopRef(UUIDUtil.newUuid()))
            .build();
        assertNotNull(schedule);
        WorkHourSchedule schedule1 = schedule.withStartTime(WorkScheduleTime.of("07:45"));
        assertNotEquals(schedule, schedule1);
        assertEquals("07:45", schedule1.getStartTime().time());
        WorkHourSchedule schedule2 = schedule1.withEndTime(WorkScheduleTime.of("17:45"));
        assertNotEquals(schedule1, schedule2);
        assertEquals("07:45", schedule2.getStartTime().time());
        assertEquals("17:45", schedule2.getEndTime().time());
        WorkHourSchedule schedule3 = schedule2.withWeekDay(WeekDay.THURSDAY);
        assertNotEquals(schedule3, schedule2);
        assertEquals("07:45", schedule3.getStartTime().time());
        assertEquals("17:45", schedule3.getEndTime().time());
        assertEquals(WeekDay.THURSDAY, schedule3.getWeekDay());
        WorkHourSchedule schedule4 = schedule3.withSopRef(new SopRef(UUIDUtil.newUuid()));
        assertNotEquals(schedule4, schedule3);
        assertEquals("07:45", schedule4.getStartTime().time());
        assertEquals("17:45", schedule4.getEndTime().time());
        assertEquals(WeekDay.THURSDAY, schedule4.getWeekDay());
        assertNotNull(schedule4.getSopRef());

    }

    @Test
    void happyPath_toString_contains_all_fields() {
        WorkHourSchedule schedule = WorkHourSchedule.builder()
            .startTime(WorkScheduleTime.of("08:00"))
            .endTime(WorkScheduleTime.of("18:00"))
            .weekDay(WeekDay.MONDAY)
            .sopRef(new SopRef(UUIDUtil.newUuid()))
            .build();

        assertNotNull(schedule);

        String result = schedule.toString();

        assertAll("toString format",
        () -> assertTrue(result.contains("WorkHourSchedule{"), "Should contain class name"),
        () -> assertTrue(result.contains("id: "+schedule.getId().value().toString()), "Should contain correct id"),
        () -> assertTrue(result.contains("startTime: "+schedule.getStartTime().time()), "Should contain start time"),
        () -> assertTrue(result.contains("endTime: "+schedule.getEndTime().time()), "Should contain end time"),
        () -> assertTrue(result.contains("weekDay: "+schedule.getWeekDay().name()), "Should contain week day"),
        () -> assertTrue(result.contains("sopRef: "+schedule.getSopRef().value().toString()), "Should contain SOP ref"),
        () -> assertTrue(result.endsWith("}"), "Should end with closing brace")
        );
    }

}
