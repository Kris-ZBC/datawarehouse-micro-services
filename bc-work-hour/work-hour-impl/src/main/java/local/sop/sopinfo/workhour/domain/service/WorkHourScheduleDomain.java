package local.sop.sopinfo.workhour.domain.service;
   /**
     * Thsi service is not a mandatory part of the CA/DDD design, 
     * but it would be used if the domain requires some special edge cases like deleting or creating a new instance
     */

import java.util.UUID;

import local.sop.common.libs.sharedkernel.enums.WeekDay;
import local.sop.sopinfo.workhour.domain.model.WorkHourSchedule;

public interface WorkHourScheduleDomain {
    WorkHourSchedule create(String startTime, String endTime, WeekDay weekDay, UUID sopRef);
    WorkHourSchedule update(UUID id, String startTime, String endTime, WeekDay weekDay, UUID sopRef);
    WorkHourSchedule delete(UUID id);
}
