package local.sop.sopinfo.workhour.application.api.dto;

import java.time.LocalTime;
import java.util.UUID;

public record WorkHourResponse(
    UUID id, 
    LocalTime startTime,
    LocalTime endTime
) {}
