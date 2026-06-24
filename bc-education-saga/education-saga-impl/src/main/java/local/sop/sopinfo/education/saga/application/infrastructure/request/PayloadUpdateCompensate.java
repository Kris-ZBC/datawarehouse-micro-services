package local.sop.sopinfo.education.saga.application.infrastructure.request;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

public record PayloadUpdateCompensate (
    UUID id, Class<?> clazz, SagaOutcome sagaState, String previousValue, Boolean previousActive
) { }