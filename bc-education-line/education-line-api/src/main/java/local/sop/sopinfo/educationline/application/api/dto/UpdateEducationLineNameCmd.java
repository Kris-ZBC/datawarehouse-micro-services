package local.sop.sopinfo.educationline.application.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateEducationLineNameCmd(
	@NotNull @NotBlank String name
) {}
