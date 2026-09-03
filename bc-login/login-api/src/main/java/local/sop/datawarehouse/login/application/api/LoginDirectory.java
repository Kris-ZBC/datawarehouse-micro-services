package local.sop.datawarehouse.login.application.api;

import local.sop.datawarehouse.login.application.api.dto.AuthenticateCmd;
import local.sop.datawarehouse.login.application.api.dto.AuthenticationResult;
import local.sop.datawarehouse.login.application.api.dto.CreateLoginCmd;
import local.sop.datawarehouse.login.application.api.dto.CreateSessionCmd;
import local.sop.datawarehouse.login.application.api.dto.CreatedLoginResult;
import local.sop.datawarehouse.login.application.api.dto.LoginResult;
import local.sop.datawarehouse.login.application.api.dto.LogoutCmd;
import local.sop.datawarehouse.login.application.api.dto.SessionValidationResult;
import local.sop.datawarehouse.login.application.api.dto.UpdateLoginStatusCmd;
import local.sop.datawarehouse.login.application.api.dto.ValidateSessionCmd;
import local.sop.common.libs.sharedkernel.sagas.compensate.Compensatable;

public interface LoginDirectory extends Compensatable{
	CreatedLoginResult createLogin(CreateLoginCmd cmd);
	AuthenticationResult authenticate(AuthenticateCmd cmd);
	LoginResult createSession(CreateSessionCmd cmd);
	void logout(LogoutCmd query);
	SessionValidationResult validateSession(ValidateSessionCmd cmd);
	void updateLoginStatus(UpdateLoginStatusCmd cmd);
}
