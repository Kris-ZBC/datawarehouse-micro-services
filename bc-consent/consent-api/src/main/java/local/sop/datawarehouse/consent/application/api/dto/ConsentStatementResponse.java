package local.sop.datawarehouse.consent.application.api.dto;

import java.util.UUID;

public record ConsentStatementResponse(
        UUID consentStatementId,
        String statementText,
        boolean active,
        String purpose,
        String type
) {
}