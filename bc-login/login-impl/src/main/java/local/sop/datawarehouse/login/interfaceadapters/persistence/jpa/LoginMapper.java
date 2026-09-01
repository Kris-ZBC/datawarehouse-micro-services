package local.sop.datawarehouse.login.interfaceadapters.persistence.jpa;

import org.springframework.stereotype.Component;

import local.sop.datawarehouse.login.domain.model.Login;
import local.sop.datawarehouse.login.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.datawarehouse.login.domain.model.valueobjects.HashedPassword;
import local.sop.datawarehouse.login.domain.model.valueobjects.LoginId;
import local.sop.datawarehouse.login.domain.model.valueobjects.PersonRef;
import local.sop.datawarehouse.login.domain.model.valueobjects.Username;

@Component
public final class LoginMapper {
	public LoginMapper() {}

	public Login toDomain(LoginEntity entity) {
		return Login.builder()
			.id(LoginId.of(entity.getId()))
			.personRef(PersonRef.of(entity.getPersonRef()))
			.username(Username.of(entity.getUsername()))
			.password(HashedPassword.of(entity.getPassword()))
			.status(entity.getStatus())
			.createdAt(CreatedAtTimestamp.of(entity.getCreatedAtTimestamp()))
			.build();
	}

	public LoginEntity toEntity(Login login) {
		return LoginEntity.builder()
			.id(login.getId().value())
			.personRef(login.getPersonRef().value())
			.username(login.getUsername().value())
			.password(login.getPassword().value())
			.status(login.getStatus())
			.createdAt(login.getCreatedAt().value())
			.build();
	}
}
