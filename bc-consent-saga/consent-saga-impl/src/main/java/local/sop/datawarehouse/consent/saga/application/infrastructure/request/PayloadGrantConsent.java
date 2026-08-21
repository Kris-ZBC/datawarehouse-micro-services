package local.sop.datawarehouse.consent.saga.application.infrastructure.request;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.enums.ConsentStatus;

// CHANGED: purpose/type removed — bc-consent's GrantConsentCmd no
// longer accepts them.
public record PayloadGrantConsent(
        UUID personRef,
        UUID consentStatementRef,
        ConsentStatus status
) {}