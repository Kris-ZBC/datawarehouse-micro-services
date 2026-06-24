package local.sop.sopinfo.workhour.application.api.dto;

import java.util.UUID;

public record FindByScheduleParamsQuery(
    UUID id, 
    String startTime,
    String endTime,
    String weekday,
    UUID sofRef


) {}
