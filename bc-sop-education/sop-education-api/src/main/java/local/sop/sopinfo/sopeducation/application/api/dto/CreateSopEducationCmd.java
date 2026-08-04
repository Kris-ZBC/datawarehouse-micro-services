package local.sop.sopinfo.sopeducation.application.api.dto;

import jakarta.validation.constraints.NotNull;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

public record CreateSopEducationCmd(
    @NotNull(message ="{key.required}" ) CompositeKey id,
    @NotNull(message ="{sop-education.active.required}" ) Boolean active
) {

}
