package local.sop.sopinfo.consent.saga.application.infrastructure.request;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

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