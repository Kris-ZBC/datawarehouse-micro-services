package local.sop.sopinfo.messageperson.application.api.dto;

import jakarta.validation.constraints.NotNull;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

public record CreateMessagePersonCmd(
    @NotNull(message = "{key.required}") CompositeKey id,
    @NotNull(message = "{message-person.active.required}") Boolean active
) {

}
