package local.sop.sopinfo.login.saga.application.infrastructure.request;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

public record PayloadCompensate(
	UUID id, 
	Class<?> clazz, 
	SagaOutcome sagaState
) { }
