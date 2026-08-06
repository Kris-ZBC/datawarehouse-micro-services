package local.sop.datawarehouse.login.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.enums.LoginStatus;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.valueobjects.utils.UUIDUtil;
import local.sop.datawarehouse.login.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.datawarehouse.login.domain.model.valueobjects.HashedPassword;
import local.sop.datawarehouse.login.domain.model.valueobjects.LoginId;
import local.sop.datawarehouse.login.domain.model.valueobjects.PersonRef;
import local.sop.datawarehouse.login.domain.model.valueobjects.Username;

public class LoginTest {

	private static final String VALID_HASH = "$2b$10$" + "a".repeat(53);

	@Test
	void shouldCreateLogin_whenAllFieldsAreValid() {
		UUID id = UUIDUtil.newUuid();
		String username = "nick579a@zbc.dk";
		LoginStatus status = LoginStatus.ACTIVATED;
		LocalDateTime now = LocalDateTime.now();

		Login login = Login.builder()
			.id(new LoginId(id))
			.personRef(new PersonRef(UUID.randomUUID()))
			.username(new Username(username))
			.password(HashedPassword.of(VALID_HASH))
			.status(status)
			.createdAt(new CreatedAtTimestamp(now))
			.build();

		assertEquals(id, login.getId().value());
		assertEquals(username, login.getUsername().value());
		assertEquals(VALID_HASH, login.getPassword().value());
		assertEquals(status, login.getStatus());
		assertEquals(now, login.getCreatedAt().value());
	}

	@Test
	void shouldGenerateId_whenIdIsNotProvided() {
		Login login = Login.builder()
			.personRef(new PersonRef(UUID.randomUUID()))
			.username(new Username("nick579a@zbc.dk"))
			.password(HashedPassword.of(VALID_HASH))
			.status(LoginStatus.ACTIVATED)
			.createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
			.build();

		assertNotNull(login.getId());
	}

	@Test
	void shouldGenerateCreatedAt_whenNotProvided() {
		Login login = Login.builder()
			.personRef(new PersonRef(UUID.randomUUID()))
			.username(new Username("nick579a@zbc.dk"))
			.password(HashedPassword.of(VALID_HASH))
			.status(LoginStatus.ACTIVATED)
			.build();

		assertNotNull(login.getCreatedAt());
	}

	@Test
	void shouldReturnNewInstance_whenPersonRefIsChanged() {
		UUID personRef = UUID.randomUUID();
		UUID newPersonRef = UUID.randomUUID();

		Login original = Login.builder()
			.personRef(new PersonRef(personRef))
			.username(new Username("nick579a@zbc.dk"))
			.password(HashedPassword.of(VALID_HASH))
			.status(LoginStatus.ACTIVATED)
			.createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
			.build();

		Login updated = original.withPersonRef(new PersonRef(newPersonRef));

		assertNotSame(original, updated);
		assertEquals(newPersonRef, updated.getPersonRef().value());
		assertEquals(personRef, original.getPersonRef().value());
	}

	@Test
	void shouldReturnNewInstance_whenUsernameIsChanged() {
		String username = "nick579a@zbc.dk";
		String newUsername = "john.doe@zbc.dk";

		Login original = Login.builder()
			.personRef(new PersonRef(UUID.randomUUID()))
			.username(new Username(username))
			.password(HashedPassword.of(VALID_HASH))
			.status(LoginStatus.ACTIVATED)
			.createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
			.build();

		Login updated = original.withUsername(new Username(newUsername));

		assertNotSame(original, updated);
		assertEquals(newUsername, updated.getUsername().value());
		assertEquals(username, original.getUsername().value());
	}

	@Test
	void shouldReturnNewInstance_whenPasswordIsChanged() {
	String newHash = "$2b$10$" + "b".repeat(53);

		Login original = Login.builder()
			.personRef(new PersonRef(UUID.randomUUID()))
			.username(new Username("nick579a@zbc.dk"))
			.password(HashedPassword.of(VALID_HASH))
			.status(LoginStatus.ACTIVATED)
			.createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
			.build();

		Login updated = original.withPassword(HashedPassword.of(newHash));

		assertEquals(newHash, updated.getPassword().value());
		assertEquals(VALID_HASH, original.getPassword().value());
	}

	@Test
	void shouldReturnNewInstance_whenStatusIsChanged() {
		LoginStatus status = LoginStatus.ACTIVATED;
		LoginStatus newStatus = LoginStatus.DEACTIVATED;

		Login original = Login.builder()
			.personRef(new PersonRef(UUID.randomUUID()))
			.username(new Username("nick579a@zbc.dk"))
			.password(HashedPassword.of("$2b$10$" + "a".repeat(53)))
			.status(LoginStatus.ACTIVATED)
			.createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
			.build();

		Login updated = original.withStatus(newStatus);

		assertNotSame(original, updated);
		assertEquals(newStatus, updated.getStatus());
		assertEquals(status, original.getStatus());
	}

	@Test
	void shouldThrowException_whenPersonRefIsNull() {
		assertThrows(ValidationException.class, () ->
			Login.builder()
				.username(new Username("nick579a@zbc.dk"))
				.password(HashedPassword.of("$2b$10$" + "a".repeat(53)))
				.createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
				.build());
	}

	@Test
	void shouldThrowException_whenUsernameIsNull() {
		assertThrows(ValidationException.class, () ->
			Login.builder()
				.personRef(new PersonRef(UUID.randomUUID()))
				.password(HashedPassword.of("$2b$10$" + "a".repeat(53)))
				.createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
				.build());
	}

	@Test
	void shouldThrowException_whenPasswordIsNull() {
		assertThrows(ValidationException.class, () ->
			Login.builder()
				.personRef(new PersonRef(UUID.randomUUID()))
				.username(new Username("nick579a@zbc.dk"))
				.createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
				.build());
	}

	@Test
	void shouldThrowException_whenStatusIsNull() {
		assertThrows(ValidationException.class, () ->
			Login.builder()
				.personRef(new PersonRef(UUID.randomUUID()))
				.username(new Username("nick579a@zbc.dk"))
				.password(HashedPassword.of("$2b$10$" + "a".repeat(53)))
				.createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
				.build());
	}
}
