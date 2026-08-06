package local.sop.datawarehouse.educationline.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateEducationLineCmd (
	@NotNull @NotBlank String name,
	@NotNull Integer durationYears,
	@NotNull Integer durationMonths,
	@NotNull Integer durationDays,
	@NotNull UUID educationRef
) {}
