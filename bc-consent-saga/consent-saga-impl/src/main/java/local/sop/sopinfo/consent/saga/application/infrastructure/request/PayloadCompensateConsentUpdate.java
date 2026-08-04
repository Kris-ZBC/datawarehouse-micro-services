package local.sop.sopinfo.consent.saga.application.infrastructure.request;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

public record PayloadCompensateConsentUpdate(
        UUID consentId,
        UUID personReference,
        UUID consentStatementId,
        String status,
        String consentPurpose,
        String consentType,
        Class<?> sagaClass,
        SagaOutcome sagaOutcome
) {}