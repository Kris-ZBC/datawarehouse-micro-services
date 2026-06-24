package local.sop.sopinfo.educationinstructor.application.api.dto;

import jakarta.validation.constraints.NotNull;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;

public record CreateEducationInstructorCmd(
    @NotNull(message = "{key.required}") CompositeKey id,
    @NotNull(message = "{educationinstructor.active.required}") Boolean active
) { }