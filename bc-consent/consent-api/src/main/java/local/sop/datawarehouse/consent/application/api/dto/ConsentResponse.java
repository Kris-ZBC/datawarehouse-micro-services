package local.sop.datawarehouse.consent.application.api.dto;

import java.util.UUID;

public record ConsentResponse(
        UUID consentId,
        UUID personReference,
        String status,
        UUID consentStatementId,
        String consentStatementText,
        String consentPurpose,      // New
        String consentType,          // New
        boolean active              // New
) {}
