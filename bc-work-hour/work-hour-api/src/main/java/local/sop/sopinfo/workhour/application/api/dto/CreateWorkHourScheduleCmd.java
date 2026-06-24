package local.sop.sopinfo.workhour.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateWorkHourScheduleCmd(
    @NotBlank(message="{time.invalid}") String startTime,
    @NotBlank(message="{time.invalid}") String endTime,
    @NotBlank(message="{weekday.invalid") String weekDay,
    @NotNull(message="{sopref.invalid}") UUID sopRef
) {}
