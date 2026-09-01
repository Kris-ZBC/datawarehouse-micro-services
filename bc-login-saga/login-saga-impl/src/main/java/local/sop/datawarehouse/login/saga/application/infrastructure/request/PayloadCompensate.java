package local.sop.datawarehouse.login.saga.application.infrastructure.request;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

public record PayloadCompensate(
	UUID id, 
	Class<?> clazz, 
	SagaOutcome sagaState
) { }
