package local.sop.datawarehouse.registration.saga.application.api.dto.person;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePersonCmd(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        @NotNull UUID organizationRef,
        List<@Valid CreatePhoneNumberCmd> phoneNumbers
) {
}