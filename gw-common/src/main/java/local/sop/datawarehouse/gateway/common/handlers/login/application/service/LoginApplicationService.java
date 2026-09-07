package local.sop.datawarehouse.gateway.common.handlers.login.application.service;

import org.springframework.stereotype.Service;
 
import local.sop.datawarehouse.gateway.common.handlers.login.api.LoginDirectory;
import local.sop.datawarehouse.gateway.common.handlers.login.api.dto.LoginRequest;
import local.sop.datawarehouse.gateway.common.handlers.login.application.ports.LoginPort;
import local.sop.datawarehouse.gateway.common.handlers.login.application.ports.LoginPort.LoginResult;
 
@Service
public class LoginApplicationService implements LoginDirectory {
 
    private final LoginPort logins;
 
    public LoginApplicationService(LoginPort logins) {
        this.logins = logins;
    }
 
    @Override
    public LoginResult login(LoginRequest request) {
        return logins.login(request.username(), request.password());
    }
 
    @Override
    public void logout(String sessionToken) {
        logins.logout(sessionToken);
    }
}
