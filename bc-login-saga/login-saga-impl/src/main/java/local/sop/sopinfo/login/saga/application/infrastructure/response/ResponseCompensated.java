package local.sop.sopinfo.login.saga.application.infrastructure.response;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

public record ResponseCompensated(
	SagaOutcome sagaState,
	Boolean success
) { }
