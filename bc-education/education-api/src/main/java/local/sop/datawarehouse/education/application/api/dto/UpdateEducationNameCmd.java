package local.sop.datawarehouse.education.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateEducationNameCmd(
    @NotNull(message="{education.name.invalid}")
    @NotBlank(message="{education.name.invalid}")
    @Size(max=100, message="{education.name.invalid}")
    String name
) {}
