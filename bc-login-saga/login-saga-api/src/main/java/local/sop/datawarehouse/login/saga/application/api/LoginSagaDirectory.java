package local.sop.datawarehouse.login.saga.application.api;

import local.sop.datawarehouse.login.saga.application.api.dto.LoginCmd;
import local.sop.datawarehouse.login.saga.application.api.dto.LoginResult;

public interface LoginSagaDirectory {
	LoginResult login(LoginCmd query);
}
