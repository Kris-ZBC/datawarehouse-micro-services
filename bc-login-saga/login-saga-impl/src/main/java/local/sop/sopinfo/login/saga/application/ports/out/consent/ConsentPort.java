package local.sop.sopinfo.login.saga.application.ports.out.consent;

import java.util.UUID;

import local.sop.sopinfo.login.saga.application.infrastructure.response.ResponseCompensated;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

public interface ConsentPort {
	boolean hasConsent(UUID personRef);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
