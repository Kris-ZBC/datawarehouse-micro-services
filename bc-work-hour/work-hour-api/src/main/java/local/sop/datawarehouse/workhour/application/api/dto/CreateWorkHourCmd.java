package local.sop.datawarehouse.workhour.application.api.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

public record CreateWorkHourCmd(
    @NotNull(message = "{workhour.starttime.required}") LocalTime startTime,
    @NotNull(message = "{workhour.endtime.required}") LocalTime endTime
) { }