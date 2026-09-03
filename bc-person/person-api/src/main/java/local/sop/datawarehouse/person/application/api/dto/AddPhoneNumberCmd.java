package local.sop.datawarehouse.person.application.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import local.sop.datawarehouse.sharedlib.enums.PhoneUserType;

public record AddPhoneNumberCmd(
        @NotNull PhoneUserType type,
        @NotBlank String value
) {
}