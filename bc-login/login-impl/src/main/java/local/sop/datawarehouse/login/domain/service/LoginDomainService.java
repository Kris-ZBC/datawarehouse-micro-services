package local.sop.datawarehouse.login.domain.service;

import java.util.UUID;

import local.sop.datawarehouse.login.domain.model.Login;
import local.sop.datawarehouse.login.domain.model.valueobjects.HashedPassword;
import local.sop.datawarehouse.login.domain.model.valueobjects.PersonRef;
import local.sop.datawarehouse.login.domain.model.valueobjects.Username;
import local.sop.datawarehouse.login.domain.model.valueobjects.IsAccepted;
import local.sop.datawarehouse.sharedlib.enums.LoginStatus;

public class LoginDomainService implements LoginDomain {

	@Override
	public Login createLogin(UUID personRef, String username, String password, LoginStatus status) {
		return Login.builder()
			.personRef(PersonRef.of(personRef))
			.username(Username.of(username))
			.password(HashedPassword.of(password))
			.status(status)
			.isActivated(IsAccepted.of(true))
			.build();
	}

}
