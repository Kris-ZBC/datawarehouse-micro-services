package local.sop.sopinfo.registration.saga.application.api.dto.person;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import local.sop.common.libs.sharedkernel.enums.PhoneUserType;

public record CreatePhoneNumberCmd(
        @NotNull PhoneUserType type,
        @NotBlank String value
) {
}