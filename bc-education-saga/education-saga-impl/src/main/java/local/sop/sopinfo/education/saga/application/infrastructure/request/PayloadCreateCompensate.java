package local.sop.sopinfo.education.saga.application.infrastructure.request;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

public record PayloadCreateCompensate (
    UUID id, Class<?> clazz, SagaOutcome sagaState
) { }