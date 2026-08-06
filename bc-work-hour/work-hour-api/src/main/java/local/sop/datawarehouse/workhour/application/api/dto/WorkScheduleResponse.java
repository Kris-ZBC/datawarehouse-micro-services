package local.sop.datawarehouse.workhour.application.api.dto;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.enums.WeekDay;

public record WorkScheduleResponse(
    UUID id, 
    String startTime,
    String endTime,
    WeekDay weekday,
    UUID sopRef
) {}
