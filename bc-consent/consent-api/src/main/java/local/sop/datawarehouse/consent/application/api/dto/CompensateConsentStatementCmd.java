package local.sop.datawarehouse.consent.application.api.dto;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

public record CompensateConsentStatementCmd(
    Class<?> clazz,
    SagaOutcome sagaState

) {}
