package local.sop.datawarehouse.login.application.api;

import local.sop.common.libs.sharedkernel.sagas.compensate.Compensatable;
import local.sop.datawarehouse.login.application.api.dto.CreateLoginCmd;
import local.sop.datawarehouse.login.application.api.dto.CreatedLoginResult;
import local.sop.datawarehouse.login.application.api.dto.LoginCmd;
import local.sop.datawarehouse.login.application.api.dto.LoginResult;
import local.sop.datawarehouse.login.application.api.dto.LogoutCmd;
import local.sop.datawarehouse.login.application.api.dto.SessionValidationResult;
import local.sop.datawarehouse.login.application.api.dto.ValidateSessionCmd;

public interface LoginDirectory extends Compensatable{
	CreatedLoginResult createLogin(CreateLoginCmd cmd);
	LoginResult login(LoginCmd query);
	void logout(LogoutCmd query);
	SessionValidationResult validateSession(ValidateSessionCmd cmd);
}
