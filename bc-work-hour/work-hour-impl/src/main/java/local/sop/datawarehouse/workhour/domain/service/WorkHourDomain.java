package local.sop.datawarehouse.workhour.domain.service;
   /**
     * Thsi service is not a mandatory part of the CA/DDD design, 
     * but it would be used if the domain requires some special edge cases like deleting or creating a new instance
     */

import java.time.LocalTime;

import local.sop.datawarehouse.workhour.domain.model.WorkHour;
import local.sop.datawarehouse.workhour.domain.model.valueobjects.WorkHourId;

public interface WorkHourDomain {
    public WorkHour create(LocalTime startTime, LocalTime endTime);
    public WorkHour update(WorkHourId id, LocalTime startTime, LocalTime endTime);

}
