package local.sop.sopinfo.educationinstructor.application.api.dto;

import jakarta.validation.constraints.NotNull;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

public record ToggleActivateEducationInstructorCmd(
    @NotNull(message = "{key.required}") CompositeKey id,
    @NotNull(message = "{educationinstructor.active.required}") Boolean active
) {

}
