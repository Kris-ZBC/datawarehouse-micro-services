package local.sop.sopinfo.workhour.application.api.dto;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.enums.WeekDay;

public record WorkScheduleResponse(
    UUID id, 
    String startTime,
    String endTime,
    WeekDay weekday,
    UUID sopRef
) {}
