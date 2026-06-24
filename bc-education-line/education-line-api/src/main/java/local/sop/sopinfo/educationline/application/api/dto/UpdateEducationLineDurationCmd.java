package local.sop.sopinfo.educationline.application.api.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateEducationLineDurationCmd(
	@NotNull Integer durationYears,
	@NotNull Integer durationMonths,
	@NotNull Integer durationDays
) {}
