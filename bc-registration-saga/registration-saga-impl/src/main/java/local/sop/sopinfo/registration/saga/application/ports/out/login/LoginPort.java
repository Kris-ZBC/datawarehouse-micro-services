package local.sop.sopinfo.registration.saga.application.ports.out.login;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.sopinfo.registration.saga.application.api.dto.login.CreateLoginCmd;
import local.sop.sopinfo.registration.saga.application.api.dto.login.LoginResponse;
import local.sop.sopinfo.registration.saga.application.infrastructure.response.ResponseLoginCreated;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

public interface LoginPort {
	ResponseLoginCreated create(CreateLoginCmd payload);
	LoginResponse getById(UUID id);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
