package local.sop.datawarehouse.gateway.admin.handlers.user.api.dto.request;

import java.util.List;
import java.util.UUID;
 
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import local.sop.common.libs.sharedkernel.enums.ConsentStatus;
 
/**
 * What the BFF sends the Handler. This is intentionally its own contract,
 * not a reuse of the Saga's CreateInstructorRegistrationCmd — the Handler
 * is the anti-corruption boundary between "what a client can reasonably
 * send" and "what the Saga needs to orchestrate the write". Keeping them
 * separate means the Saga's DTO can evolve (e.g. new BC touched, new
 * compensation field) without forcing every BFF/frontend to change too.
 */

public record RegisterInstructorRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank @Email String email,
    @NotNull UUID organizationRef,
    List<@Valid PhoneNumber> phoneNumbers,
    @NotBlank String username,
    List<@Valid ConsentStatement> consentStatements
) {
        public record PhoneNumber(@NotBlank String type, @NotBlank String value) {
        }
        public record ConsentStatement(@NotNull UUID consentStatementRef, @NotNull ConsentStatus status) {}

}
