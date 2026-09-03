package local.sop.datawarehouse.login.interfaceadapters.persistence.jpa;

import org.springframework.stereotype.Component;

import local.sop.datawarehouse.login.domain.model.Login;
import local.sop.datawarehouse.login.domain.model.Session;
import local.sop.datawarehouse.login.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.datawarehouse.login.domain.model.valueobjects.ExpiresAtTimestamp;
import local.sop.datawarehouse.login.domain.model.valueobjects.HashedPassword;
import local.sop.datawarehouse.login.domain.model.valueobjects.LoginId;
import local.sop.datawarehouse.login.domain.model.valueobjects.PersonRef;
import local.sop.datawarehouse.login.domain.model.valueobjects.SessionId;
import local.sop.datawarehouse.login.domain.model.valueobjects.SessionToken;
import local.sop.datawarehouse.login.domain.model.valueobjects.Username;

@Component
public final class SessionMapper {
	public SessionMapper() {}

	public Session toDomain(SessionEntity entity) {
		LoginEntity loginEntity = entity.getLogin();
		Login login = Login.builder()
			.id(LoginId.of(loginEntity.getId()))
			.personRef(PersonRef.of(loginEntity.getPersonRef()))
			.username(Username.of(loginEntity.getUsername()))
			.password(HashedPassword.of(loginEntity.getPassword()))
			.status(loginEntity.getStatus())
			.createdAt(CreatedAtTimestamp.of(loginEntity.getCreatedAtTimestamp()))
			.build();
		return Session.builder()
			.id(SessionId.of(entity.getId()))
			.login(login)
			.sessionToken(SessionToken.of(entity.getSessionToken()))
			.createdAt(CreatedAtTimestamp.of(entity.getCreatedAt()))
			.expiresAt(ExpiresAtTimestamp.of(entity.getExpiresAt()))
			.role(entity.getRole())
			.build();
	}

	public SessionEntity toEntity(Session session, LoginEntity loginEntity) {
		return SessionEntity.builder()
			.id(session.getId().value())
			.login(loginEntity)
			.sessionToken(session.getToken().value())
			.createdAt(session.getCreatedAt().value())
			.expiresAt(session.getExpiresAt().value())
			.role(session.getRole())
			.build();
	}
}