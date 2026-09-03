package local.sop.datawarehouse.registration.saga.application.ports.out.login;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.registration.saga.application.api.dto.login.CreateLoginCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.login.LoginResponse;
import local.sop.datawarehouse.registration.saga.application.infrastructure.response.ResponseLoginCreated;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

public interface LoginPort {
	ResponseLoginCreated create(CreateLoginCmd payload);
	LoginResponse getById(UUID id);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
	
	// NEW: general-purpose Login disable — used here specifically to
	// disable the tech user once an instructor is confirmed created.
	// Best-effort from the caller's side (see
	// RegistrationSagaApplicationService): a failure here should not
	// roll back an otherwise-successful instructor registration.
	void disableLogin(UUID loginId);
}
