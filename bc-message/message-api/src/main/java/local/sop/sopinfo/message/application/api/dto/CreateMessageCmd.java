package local.sop.sopinfo.message.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMessageCmd(

    @NotNull(message = "message.senderPersonRef.required")
    UUID senderPersonRef,

    @NotBlank(message = "message.message.required")
    @Size(max = 4000, message = "message.message.maxLength")
    String message
) {
}