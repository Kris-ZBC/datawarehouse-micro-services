package local.sop.datawarehouse.consent.application.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateConsentStatementCmd(
    @NotNull(message= "{consentstatement.active.required}") Boolean active,
    @NotNull(message= "{consent.statementtext.invalid}")
    @NotBlank(message= "{consent.statementtext.invalid}") 
    @Size(max=1000, message= "{consent.statementtext.length.invalid}")
    String statementText
) {}
