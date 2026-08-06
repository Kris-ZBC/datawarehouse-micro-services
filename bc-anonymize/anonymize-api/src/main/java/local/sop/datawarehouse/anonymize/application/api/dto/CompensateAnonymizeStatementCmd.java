package local.sop.datawarehouse.anonymize.application.api.dto;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

public record CompensateAnonymizeStatementCmd(
    Class<?> clazz,
    SagaOutcome sagaState

) {}
