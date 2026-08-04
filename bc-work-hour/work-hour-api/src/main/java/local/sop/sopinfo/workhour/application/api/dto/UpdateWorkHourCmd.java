package local.sop.sopinfo.workhour.application.api.dto;

import java.time.LocalTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record UpdateWorkHourCmd( 
    @NotNull(message = "{workhour.id.required}") UUID id,
    @NotNull(message = "{workhour.starttime.required}") LocalTime startTime,
    @NotNull(message = "{workhour.endtime.required}") LocalTime endTime
) {}
