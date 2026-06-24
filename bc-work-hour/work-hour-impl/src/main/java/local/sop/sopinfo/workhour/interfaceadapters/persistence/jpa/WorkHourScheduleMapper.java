package local.sop.sopinfo.workhour.interfaceadapters.persistence.jpa;

import org.springframework.stereotype.Component;

import local.sop.sopinfo.workhour.domain.model.WorkHourSchedule;
import local.sop.sopinfo.workhour.domain.model.valueobjects.SopRef;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkScheduleId;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkScheduleTime;

@Component
public final class WorkHourScheduleMapper {
    private WorkHourScheduleMapper() {}

    /** JPA -> Domain */
    public WorkHourSchedule toDomain(WorkHourScheduleEntity e) {
        return WorkHourSchedule.builder()
            .id(WorkScheduleId.of(e.getId()))
            .startTime(WorkScheduleTime.of(e.getStartTime()))
            .endTime(WorkScheduleTime.of(e.getEndTime()))
            .weekDay(e.getWeekDay())
            .sopRef(SopRef.of(e.getSopRef()))
            .build();            
    }

    /** Domain -> JPA */
    public WorkHourScheduleEntity toEntity(WorkHourSchedule s) {
        return WorkHourScheduleEntity.builder()
            .id(s.getId().value())
            .startTime(s.getStartTime().time())
            .endTime(s.getEndTime().time())
            .weekDay(s.getWeekDay())
            .sopRef(s.getSopRef().value())
            .build();
    }

    /** Domain -> existing JPA (Update) */
    public void copyIntoEntity(WorkHourSchedule s, WorkHourScheduleEntity se) {
        se
        .withStartTime(s.getStartTime().time())
        .withEndTime(s.getEndTime().time())
        .withWeekDay(s.getWeekDay())
        .withSopRef(s.getSopRef().value());
    }
}
