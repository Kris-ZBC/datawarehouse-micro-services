package local.sop.sopinfo.login.saga.application.ports.out.login;

import java.util.UUID;

import local.sop.sopinfo.login.saga.application.api.dto.LoginResult;
import local.sop.sopinfo.login.saga.application.infrastructure.response.ResponseCompensated;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

public interface LoginPort {
	LoginResult login(String username, String password);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
