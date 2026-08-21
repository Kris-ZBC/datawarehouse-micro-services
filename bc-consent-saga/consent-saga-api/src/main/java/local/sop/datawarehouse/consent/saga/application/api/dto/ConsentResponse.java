package local.sop.datawarehouse.consent.saga.application.api.dto;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentType;

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
