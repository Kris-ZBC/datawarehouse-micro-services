package local.sop.datawarehouse.login.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.datawarehouse.login.domain.model.Login;
import local.sop.datawarehouse.login.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.datawarehouse.login.domain.model.valueobjects.HashedPassword;
import local.sop.datawarehouse.login.domain.model.valueobjects.LoginId;
import local.sop.datawarehouse.login.domain.model.valueobjects.PersonRef;
import local.sop.datawarehouse.login.domain.model.valueobjects.Username;
import local.sop.datawarehouse.sharedlib.enums.LoginStatus;

public class LoginMapperTest {
	
	private final LoginMapper mapper = new LoginMapper();

	@Test
	void shouldMapToDomain() {
		LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
		LoginEntity entity = LoginEntity.builder()
			.id(UUID.randomUUID())
			.personRef(UUID.randomUUID())
			.username("nick579a@zbc.dk")
			.password("$2b$10$" + "a".repeat(53))  // ← valid bcrypt hash
			.status(LoginStatus.ACTIVATED)
			.createdAt(createdAt)
			.build();

		Login login = mapper.toDomain(entity);

		assertEquals(entity.getId(), login.getId().value());
		assertEquals(entity.getPersonRef(), login.getPersonRef().value());
		assertEquals(entity.getUsername(), login.getUsername().value());
		assertEquals(entity.getPassword(), login.getPassword().value());
		assertEquals(entity.getStatus(), login.getStatus());
		assertEquals(createdAt, login.getCreatedAt().value());
	}

	@Test
	void shouldMapToEntity() {
		LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
		Login login = Login.builder()
			.id(LoginId.of(UUID.randomUUID()))
			.personRef(PersonRef.of(UUID.randomUUID()))
			.username(Username.of("nick579a@zbc.dk"))
			.password(HashedPassword.of("$2b$10$" + "a".repeat(53))) 
			.status(LoginStatus.ACTIVATED)
			.createdAt(new CreatedAtTimestamp(createdAt))
			.build();

		LoginEntity entity = mapper.toEntity(login);

		assertEquals(login.getId().value(), entity.getId());
		assertEquals(login.getPersonRef().value(), entity.getPersonRef());
		assertEquals(login.getUsername().value(), entity.getUsername());
		assertEquals(login.getPassword().value(), entity.getPassword());
		assertEquals(login.getStatus(), entity.getStatus());
		assertEquals(createdAt, entity.getCreatedAtTimestamp());
	}
}
