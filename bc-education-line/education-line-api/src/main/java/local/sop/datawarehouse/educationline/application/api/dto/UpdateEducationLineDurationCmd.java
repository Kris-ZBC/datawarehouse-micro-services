package local.sop.datawarehouse.educationline.application.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateEducationLineDurationCmd(
	@Min(value = 0, message = "{educationline.duration.years.min}")
	@NotNull(message = "{educationline.duration.years.required}") Integer durationYears,

	@Min(value = 0, message = "{educationline.duration.months.min}")
	@Max(value = 11, message = "{educationline.duration.months.max}")
	@NotNull(message = "{educationline.duration.months.required}") Integer durationMonths,

	@Min(value = 0, message = "{educationline.duration.days.min}")
	@Max(value = 30, message = "{educationline.duration.days.max}")
	@NotNull(message = "{educationline.duration.days.required}") Integer durationDays
) {}
