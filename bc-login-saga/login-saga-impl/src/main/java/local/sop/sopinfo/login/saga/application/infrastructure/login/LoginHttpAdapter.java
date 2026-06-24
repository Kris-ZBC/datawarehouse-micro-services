package local.sop.sopinfo.login.saga.application.infrastructure.login;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.login.saga.application.api.dto.LoginResult;
import local.sop.sopinfo.login.saga.application.infrastructure.request.PayloadCompensate;
import local.sop.sopinfo.login.saga.application.infrastructure.request.PayloadLogin;
import local.sop.sopinfo.login.saga.application.infrastructure.response.ResponseCompensated;
import local.sop.sopinfo.login.saga.application.ports.out.login.LoginPort;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

@Component
public class LoginHttpAdapter implements LoginPort {
	
	private final RestClient login;

	public LoginHttpAdapter(@Qualifier("login") RestClient login) {
		this.login = login;
	}

	@Override
	public LoginResult login(String username, String password) {
		return login.post()
			.uri("/internal/logins/sessions/login")
			.body(new PayloadLogin(username, password))
			.retrieve()
			.body(LoginResult.class);
	}

	@Override
    public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        return login.post()
            .uri("/internal/logins/compensate")
            .body(new PayloadCompensate(id, clazz, sagaState))
            .retrieve()
            .body(ResponseCompensated.class);
    }
}
