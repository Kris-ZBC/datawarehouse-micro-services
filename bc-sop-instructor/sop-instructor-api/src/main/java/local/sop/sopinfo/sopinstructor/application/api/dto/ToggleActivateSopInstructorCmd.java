package local.sop.sopinfo.sopinstructor.application.api.dto;

import jakarta.validation.constraints.NotNull;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

public record ToggleActivateSopInstructorCmd(
    @NotNull(message = "{key.required}") CompositeKey id,
    @NotNull(message = "{sop-instructor.active.required}") Boolean active
) {

}
