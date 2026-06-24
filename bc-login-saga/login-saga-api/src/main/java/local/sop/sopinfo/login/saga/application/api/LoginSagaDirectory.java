package local.sop.sopinfo.login.saga.application.api;

import local.sop.sopinfo.login.saga.application.api.dto.LoginResult;
import local.sop.sopinfo.login.saga.application.api.dto.LoginCmd;

public interface LoginSagaDirectory {
	LoginResult login(LoginCmd query);
}
