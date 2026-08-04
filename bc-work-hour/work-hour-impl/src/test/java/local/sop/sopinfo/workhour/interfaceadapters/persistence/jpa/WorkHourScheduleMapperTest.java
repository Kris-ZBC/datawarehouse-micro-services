package local.sop.sopinfo.workhour.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.common.libs.sharedkernel.enums.WeekDay;
import local.sop.common.libs.sharedkernel.valueobjects.utils.UUIDUtil;
import local.sop.sopinfo.workhour.domain.model.WorkHourSchedule;
import local.sop.sopinfo.workhour.domain.model.valueobjects.SopRef;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkScheduleId;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkScheduleTime;

@ExtendWith(MockitoExtension.class)
public class WorkHourScheduleMapperTest {

    @InjectMocks
    private WorkHourScheduleMapper mapper;

    @Mock
    private WorkHourScheduleEntity entity;

    @Test
    void toDomain_shouldMapAllFields() {
        UUID id = UUIDUtil.newUuid();
        UUID sopRef = UUIDUtil.newUuid();

        when(entity.getId()).thenReturn(id);
        when(entity.getStartTime()).thenReturn("07:00");
        when(entity.getEndTime()).thenReturn("16:00");
        when(entity.getWeekDay()).thenReturn(WeekDay.MONDAY);
        when(entity.getSopRef()).thenReturn(sopRef);

        WorkHourSchedule domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(id, domain.getId().value());
        assertEquals("07:00", domain.getStartTime().time());
        assertEquals("16:00", domain.getEndTime().time());
        assertEquals(WeekDay.MONDAY, domain.getWeekDay());
        assertEquals(sopRef, domain.getSopRef().value());
    }

    @Test
    void toEntity_shouldMapAllFields() {
        UUID id = UUIDUtil.newUuid();
        UUID sopRef = UUIDUtil.newUuid();

        WorkHourSchedule domain = WorkHourSchedule.builder()
            .id(WorkScheduleId.of(id))
            .startTime(WorkScheduleTime.of("08:30"))
            .endTime(WorkScheduleTime.of("17:30"))
            .weekDay(WeekDay.FRIDAY)
            .sopRef(SopRef.of(sopRef))
            .build();

        WorkHourScheduleEntity result = mapper.toEntity(domain);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("08:30", result.getStartTime());
        assertEquals("17:30", result.getEndTime());
        assertEquals(WeekDay.FRIDAY, result.getWeekDay());
        assertEquals(sopRef, result.getSopRef());
    }

    @Test
    void copyIntoEntity_shouldApplyAllFields() {
        UUID id = UUIDUtil.newUuid();
        UUID sopRef = UUIDUtil.newUuid();

        WorkHourSchedule domain = WorkHourSchedule.builder()
            .id(WorkScheduleId.of(id))
            .startTime(WorkScheduleTime.of("08:30"))
            .endTime(WorkScheduleTime.of("17:30"))
            .weekDay(WeekDay.FRIDAY)
            .sopRef(SopRef.of(sopRef))
            .build();

        when(entity.withStartTime("08:30")).thenReturn(entity);
        when(entity.withEndTime("17:30")).thenReturn(entity);
        when(entity.withWeekDay(WeekDay.FRIDAY)).thenReturn(entity);
        when(entity.withSopRef(sopRef)).thenReturn(entity);

        mapper.copyIntoEntity(domain, entity);

        verify(entity).withStartTime("08:30");
        verify(entity).withEndTime("17:30");
        verify(entity).withWeekDay(WeekDay.FRIDAY);
        verify(entity).withSopRef(sopRef);
    }

}
