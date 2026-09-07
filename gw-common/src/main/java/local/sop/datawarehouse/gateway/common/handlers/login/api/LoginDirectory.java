package local.sop.datawarehouse.gateway.common.handlers.login.api;

import local.sop.datawarehouse.gateway.common.handlers.login.api.dto.LoginRequest;
import local.sop.datawarehouse.gateway.common.handlers.login.application.ports.LoginPort.LoginResult;

// Returns the full LoginPort.LoginResult (includes sessionToken) to
// the controller, which decides what goes in the JSON body vs the
// cookie — LoginResponse (the wire-safe shape, no token) is built by
// the controller itself, not returned from here.
public interface LoginDirectory {
    LoginResult login(LoginRequest request);
    void logout(String sessionToken);
}
