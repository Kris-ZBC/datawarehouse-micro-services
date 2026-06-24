package local.sop.sopinfo.consent.application.api.dto;

import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

public record CompensateConsentStatementCmd(
    Class<?> clazz,
    SagaOutcome sagaState

) {}
