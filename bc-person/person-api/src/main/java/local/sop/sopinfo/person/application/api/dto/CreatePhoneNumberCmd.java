package local.sop.sopinfo.person.application.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import local.sop.sopinfo.sharedkernel.enums.PhoneUserType;

public record CreatePhoneNumberCmd(
        @NotNull PhoneUserType type,
        @NotBlank String value
) {
}