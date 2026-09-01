package local.sop.datawarehouse.consent.saga.application.api.dto;

import java.util.UUID;

import local.sop.datawarehouse.sharedlib.enums.ConsentPurpose;
import local.sop.datawarehouse.sharedlib.enums.ConsentType;

public record ConsentResponse(
        UUID consentId,
        UUID personReference,
        String status,
        UUID consentStatementId,
        String consentStatementText,
        ConsentPurpose consentStatementPurpose,
        ConsentType consentStatementType,
        boolean active
) {}
