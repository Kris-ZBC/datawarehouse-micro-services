package local.sop.datawarehouse.workhour.interfaceadapters.persistence.jpa;

import org.springframework.stereotype.Component;

import local.sop.datawarehouse.workhour.domain.model.WorkHour;
import local.sop.datawarehouse.workhour.domain.model.valueobjects.WorkHourId;

@Component
public final class WorkHourMapper {

    public WorkHourMapper() {}

    /** JPA -> Domain */
    public WorkHour toDomain(WorkHourEntity entity) {
        return WorkHour.builder()
            .id(WorkHourId.of(entity.getId()))
            .startTime(entity.getStartTime())
            .endTime(entity.getEndTime())
            .build();
    }

    /** Domain -> JPA */
    public WorkHourEntity toEntity(WorkHour workHour) {
        return WorkHourEntity.builder()
            .id(workHour.getId().value())
            .startTime(workHour.getStartTime())
            .endTime(workHour.getEndTime())
            .build();
    }

    /** Domain -> existing JPA (Update) */
    public void copyIntoEntity(WorkHour workHour, WorkHourEntity entity) {
        entity
            .withStartTime(workHour.getStartTime())
            .withEndTime(workHour.getEndTime());
    }
}