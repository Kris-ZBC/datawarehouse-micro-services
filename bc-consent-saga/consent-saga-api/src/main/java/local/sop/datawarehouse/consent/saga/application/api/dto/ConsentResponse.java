package local.sop.datawarehouse.consent.saga.application.api.dto;

import java.util.UUID;

public record ConsentResponse(
        UUID consentId,
        UUID personReference,
        String status,
        UUID consentStatementId,
        String consentStatementText,
        String consentPurpose,
        String consentType,
        boolean active
) {}
