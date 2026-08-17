package local.sop.datawarehouse.login.saga.application.api;

import local.sop.datawarehouse.login.saga.application.api.dto.LoginResult;
import local.sop.datawarehouse.login.saga.application.api.dto.LoginCmd;

public interface LoginSagaDirectory {
	LoginResult login(LoginCmd query);
}
