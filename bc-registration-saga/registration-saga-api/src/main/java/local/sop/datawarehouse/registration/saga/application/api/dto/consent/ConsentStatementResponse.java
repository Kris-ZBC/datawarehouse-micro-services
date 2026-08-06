package local.sop.datawarehouse.registration.saga.application.api.dto.consent;

import java.util.UUID;

public record ConsentStatementResponse(
        UUID consentStatementId,
        String statementText,
        boolean active
) {
}