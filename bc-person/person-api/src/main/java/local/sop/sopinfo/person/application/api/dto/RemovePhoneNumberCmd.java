package local.sop.sopinfo.person.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record RemovePhoneNumberCmd(
        @NotNull UUID personId,
        @NotNull UUID phoneNumberId
) {
}