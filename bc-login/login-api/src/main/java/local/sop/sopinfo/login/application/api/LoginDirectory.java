package local.sop.sopinfo.login.application.api;

import local.sop.sopinfo.login.application.api.dto.CreateLoginCmd;
import local.sop.sopinfo.login.application.api.dto.CreatedLoginResult;
import local.sop.sopinfo.login.application.api.dto.LoginCmd;
import local.sop.sopinfo.login.application.api.dto.LoginResult;
import local.sop.sopinfo.login.application.api.dto.LogoutCmd;
import local.sop.sopinfo.login.application.api.dto.SessionValidationResult;
import local.sop.sopinfo.login.application.api.dto.ValidateSessionCmd;
import local.sop.sopinfo.sharedkernel.sagas.compensate.Compensatable;

public interface LoginDirectory extends Compensatable{
	CreatedLoginResult createLogin(CreateLoginCmd cmd);
	LoginResult login(LoginCmd query);
	void logout(LogoutCmd query);
	SessionValidationResult validateSession(ValidateSessionCmd cmd);
}
