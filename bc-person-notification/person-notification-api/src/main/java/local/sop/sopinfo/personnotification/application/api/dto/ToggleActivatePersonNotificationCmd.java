package local.sop.sopinfo.personnotification.application.api.dto;

import jakarta.validation.constraints.NotNull;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

public record ToggleActivatePersonNotificationCmd(
    @NotNull(message = "{key.required}") CompositeKey id,
    @NotNull(message = "{personnotification.active.required}") Boolean active
) {

}
