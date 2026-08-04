package local.sop.sopinfo.workhour.domain.service;

import java.time.LocalTime;

import local.sop.sopinfo.workhour.domain.model.WorkHour;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkHourId;


public class WorkHourDomainService implements WorkHourDomain{


    @Override
    public WorkHour create(LocalTime startTime, LocalTime endTime) throws RuntimeException {
        return WorkHour.builder()
            .id(WorkHourId.newId())
            .startTime(startTime)
            .endTime(endTime)
            .build();
    }

    @Override 
    public WorkHour update(WorkHourId id, LocalTime startTime, LocalTime endTime) throws RuntimeException {
       return WorkHour.builder()
            .id(id)
            .startTime(startTime)
            .endTime(endTime)
            .build();
    }
}
