package local.sop.sopinfo.workhour.domain.service;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.enums.WeekDay;
import local.sop.common.libs.sharedkernel.valueobjects.utils.UUIDUtil;
import local.sop.sopinfo.workhour.domain.model.WorkHourSchedule;
import local.sop.sopinfo.workhour.domain.model.valueobjects.SopRef;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkScheduleId;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkScheduleTime;

public class WorkHourScheduleDomainService implements WorkHourScheduleDomain{


    @Override
    public WorkHourSchedule create(String startTime, String endTime, WeekDay weekDay, UUID sopRef) throws RuntimeException {
        /**
         * Any edge cases to handle?
         * 
         */
        return WorkHourSchedule.builder()
            .startTime(WorkScheduleTime.of(startTime))
            .endTime(WorkScheduleTime.of(endTime))
            .weekDay(weekDay)
            .sopRef(SopRef.of(sopRef)).build();
    }

    @Override 
    public WorkHourSchedule update(UUID id, String startTime, String endTime, WeekDay weekDay, UUID sopRef) throws RuntimeException {
        /**
         * Any edge cases to handle?
         */
       return WorkHourSchedule.builder()
            .id(WorkScheduleId.of(id))
            .startTime(WorkScheduleTime.of(startTime))
            .endTime(WorkScheduleTime.of(endTime))
            .weekDay(weekDay)
            .sopRef(SopRef.of(sopRef)).build();
    }

    @Override
    public WorkHourSchedule delete(UUID id) throws RuntimeException{
        /**
         * Any edge cases to handle?
         */
        return workHourScheduleWithKey(id);
    }

    private WorkHourSchedule workHourScheduleWithKey(UUID id) {
        return WorkHourSchedule.builder()
            .id(WorkScheduleId.of(id))
            .startTime(WorkScheduleTime.of("00:00"))
            .endTime(WorkScheduleTime.of("00:00"))
            .weekDay(WeekDay.FRIDAY)
            .sopRef(SopRef.of(UUIDUtil.newUuid())).build();
        
    }

}
