package local.sop.sopinfo.consent.saga.application.infrastructure.request;

import java.util.UUID;

public record PayloadRevokeConsent(
        UUID consentId
) {}