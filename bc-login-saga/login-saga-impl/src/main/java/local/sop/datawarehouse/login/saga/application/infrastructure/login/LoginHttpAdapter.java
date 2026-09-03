package local.sop.datawarehouse.login.saga.application.infrastructure.login;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.datawarehouse.login.saga.application.api.dto.LoginResult;
import local.sop.datawarehouse.login.saga.application.infrastructure.request.PayloadAuthenticate;
import local.sop.datawarehouse.login.saga.application.infrastructure.request.PayloadCreateSession;
import local.sop.datawarehouse.login.saga.application.infrastructure.request.PayloadLogout;
import local.sop.datawarehouse.login.saga.application.ports.out.login.LoginPort;
import local.sop.datawarehouse.sharedlib.enums.UserRole;

@Component
public class LoginHttpAdapter implements LoginPort {
	
	private final RestClient login;

	public LoginHttpAdapter(@Qualifier("login") RestClient login) {
		this.login = login;
	}

	@Override
	public AuthenticationResult authenticate(String username, String password) {
		return login.post()
			.uri("/internal/logins/sessions/authenticate")
			.body(new PayloadAuthenticate(username, password))
			.retrieve()
			.body(AuthenticationResult.class);
	}

	@Override
	public LoginResult createSession(UUID loginId, UserRole role) {
		return login.post()
			.uri("/internal/logins/sessions")
			.body(new PayloadCreateSession(loginId, role))
			.retrieve()
			.body(LoginResult.class);
	}

	@Override
	public void logout(String sessionToken) {
		login.post()
			.uri("/internal/logins/sessions/logout")
			.body(new PayloadLogout(sessionToken))
			.retrieve()
			.toBodilessEntity();
	}
}