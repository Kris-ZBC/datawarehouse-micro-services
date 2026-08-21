package local.sop.datawarehouse.consent.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentType;

// CHANGED: consentPurpose/consentType were previously untyped Strings
// marked "New - optional" and were never actually read by the service
// (dead fields). Now proper enums, and actually wired through to
// ConsentStatementRepositoryPort.updateStatement(). Still optional —
// null means "leave the existing value unchanged".
public record UpdateConsentStatementCmd(
        @NotNull(message="{consent.consentstatement.id.invalid}") UUID consentStatementId,
        @NotNull(message="{consent.statementtext.invalid}") 
        @NotBlank(message="{consent.statementtext.invalid}") 
        @Size(max=1000, message="{consent.statementtext.length.invalid}")
        String statementText,
        ConsentPurpose purpose,
        ConsentType type
) {
}