package local.sop.datawarehouse.registration.saga.application.infrastructure.login;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.registration.saga.application.api.dto.login.CreateLoginCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.login.LoginResponse;
import local.sop.datawarehouse.registration.saga.application.infrastructure.response.ResponseLoginCreated;
import local.sop.datawarehouse.registration.saga.application.ports.out.login.LoginPort;

@Component
public class LoginHttpAdapter implements LoginPort {
	
	private final RestClient login;

	public LoginHttpAdapter(@Qualifier("login") RestClient login) {
		this.login = login;
	}

	@Override
    public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        return login.put()
            .uri("/internal/logins/{id}/compensate/create", id)
            .body(new PayloadCompensateCreate(clazz, sagaState))
            .retrieve()
            .body(ResponseCompensated.class);
    }

    @Override
    public ResponseLoginCreated create(CreateLoginCmd payload) {
        return login.post()
            .uri("/internal/logins")
            .body(payload)
            .retrieve()
            .body(ResponseLoginCreated.class);
    }

    @Override
    public LoginResponse getById(UUID id) {
        return login.get()
            .uri("/internal/logins/{id}", id)
            .retrieve()
            .body(LoginResponse.class);
    }
}
