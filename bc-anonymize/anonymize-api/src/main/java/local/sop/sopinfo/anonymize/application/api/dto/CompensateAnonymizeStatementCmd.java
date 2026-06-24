package local.sop.sopinfo.anonymize.application.api.dto;

import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

public record CompensateAnonymizeStatementCmd(
    Class<?> clazz,
    SagaOutcome sagaState

) {}
