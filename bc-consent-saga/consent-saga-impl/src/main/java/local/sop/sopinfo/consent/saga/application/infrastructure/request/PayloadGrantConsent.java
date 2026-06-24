package local.sop.sopinfo.consent.saga.application.infrastructure.request;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.enums.ConsentPurpose;
import local.sop.sopinfo.sharedkernel.enums.ConsentStatus;
import local.sop.sopinfo.sharedkernel.enums.ConsentType;

public record PayloadGrantConsent(
        UUID personRef,
        UUID consentStatementRef,
        ConsentPurpose purpose,
        ConsentType type,
        ConsentStatus status
) {}