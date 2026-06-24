package local.sop.sopinfo.consent.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateConsentStatementCmd(
        @NotNull(message="{consent.consentstatement.id.invalid}") UUID consentStatementId,
        @NotNull(message="{consent.statementtext.invalid}") 
        @NotBlank(message="{consent.statementtext.invalid}") 
        @Size(max=1000, message="{consent.statementtext.length.invalid}")
        String statementText,
        String consentPurpose,      // New - optional
        String consentType          // New - optional
) {
}
