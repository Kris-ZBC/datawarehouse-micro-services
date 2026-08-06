package local.sop.datawarehouse.login.saga.application.ports.out.consent;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.datawarehouse.login.saga.application.infrastructure.response.ResponseCompensated;

public interface ConsentPort {
	boolean hasConsent(UUID personRef);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
