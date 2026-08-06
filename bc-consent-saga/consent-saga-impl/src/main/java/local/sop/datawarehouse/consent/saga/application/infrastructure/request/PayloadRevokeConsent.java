package local.sop.datawarehouse.consent.saga.application.infrastructure.request;

import java.util.UUID;

public record PayloadRevokeConsent(
        UUID consentId
) {}