package local.sop.sopinfo.consent.saga.application.infrastructure.request;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.enums.ConsentType;

public record PayloadGrantConsent(
        UUID personRef,
        UUID consentStatementRef,
        ConsentPurpose purpose,
        ConsentType type,
        ConsentStatus status
) {}