package local.sop.datawarehouse.gateway.common.handlers.login.application.infrastructure;

import java.util.UUID;
 
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
 
import local.sop.datawarehouse.sharedlib.enums.ActorType;
import local.sop.datawarehouse.sharedlib.enums.Severity;
import local.sop.datawarehouse.gateway.common.handlers.login.application.ports.LoginPort;
 
@Component
public class LoginAdapter implements LoginPort {
 
    private final RestClient loginSaga;
    private final RestClient login;
 
    public LoginAdapter(@Qualifier("login-saga") RestClient loginSaga, @Qualifier("login") RestClient login) {
        this.loginSaga = loginSaga;
        this.login = login;
    }
 
    @Override
    public LoginResult login(String username, String password) {
        return loginSaga.post()
                .uri("/internal/saga/logins/sessions/login")
                .body(new PayloadLoginCmd(username, password, UUID.randomUUID(), ActorType.USER, Severity.INFO,
                        "gw-common", "login-handler", "LoginHandler", "login attempt", "User login via common gateway"))
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
 
    private record PayloadLoginCmd(String username, String password, UUID actorRef, ActorType actorType,
            Severity severity, String originSystem, String originService, String originComponent, String data,
            String description) {}
 
    private record PayloadLogout(String sessionToken) {}
}
