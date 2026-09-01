package local.sop.datawarehouse.consent.application.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import local.sop.datawarehouse.sharedlib.enums.ConsentPurpose;
import local.sop.datawarehouse.sharedlib.enums.ConsentType;

// CHANGED: purpose and type added — required at creation, since they're
// now inherent properties of the statement itself (see ConsentStatement's
// domain model), not something the granter supplies per grant.
public record CreateConsentStatementCmd(
    @NotNull(message= "{consentstatement.active.required}") Boolean active,
    @NotNull(message= "{consent.statementtext.invalid}")
    @NotBlank(message= "{consent.statementtext.invalid}") 
    @Size(max=1000, message= "{consent.statementtext.length.invalid}")
    String statementText,
    @NotNull(message = "{consentstatement.purpose.invalid}") ConsentPurpose purpose,
    @NotNull(message = "{consentstatement.type.invalid}") ConsentType type
) {}
