package local.sop.sopinfo.person.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import local.sop.common.libs.sharedkernel.enums.PhoneUserType;

public record AddPhoneNumberCmd(
        @NotNull UUID personId,
        @NotNull PhoneUserType type,
        @NotBlank String value
) {
}