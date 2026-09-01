package local.sop.datawarehouse.login.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import local.sop.common.libs.sharedkernel.enums.LoginStatus;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

@DataJpaTest
@ActiveProfiles({"test", "h2"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LoginEntityTest {
	@Autowired
	private EntityManager entityManager;

	@Test
	void testLoginEntity() {
		UUID personRef = UUID.randomUUID();
		String username = "nick579a";
		String password = "Password5!";
		LoginStatus status = LoginStatus.ACTIVATED;

		LoginEntity entity = LoginEntity.builder()
			.id(UUID.randomUUID())
			.personRef(personRef)
			.username(username)
			.password(password)
			.status(status)
			.build();
		
		entityManager.persist(entity);
		entityManager.flush();

		LoginEntity found = entityManager.find(LoginEntity.class, entity.getId());

		assertNotNull(found);
		assertEquals(username, found.getUsername());
		assertEquals(password, found.getPassword());
		assertEquals(personRef, found.getPersonRef());
		assertEquals(status, found.getStatus());
		assertNotNull(found.getCreatedAtTimestamp());
	}

	@Test
	void shouldReturnNewInstance_whenPersonRefChanged() {
		LoginEntity original = LoginEntity.builder()
			.id(UUID.randomUUID())
			.personRef(UUID.randomUUID())
			.username("nick579a")
			.password("Password5!")
			.status(LoginStatus.ACTIVATED)
			.build();

		UUID newPersonRef = UUID.randomUUID();
		LoginEntity updated = original.withPersonRef(newPersonRef);

		assertNotSame(original, updated);
		assertEquals(newPersonRef, updated.getPersonRef());
		assertEquals(original.getId(), updated.getId());
	}


	@Test
	void shouldReturnNewInstance_whenUsernameChanged() {
		LoginEntity original = LoginEntity.builder()
			.id(UUID.randomUUID())
			.personRef(UUID.randomUUID())
			.username("nick579a")
			.password("Password5!")
			.status(LoginStatus.ACTIVATED)
			.build();

		String newUsername = "newUsername";
		LoginEntity updated = original.withUsername(newUsername);

		assertNotSame(original, updated);
		assertEquals("newUsername", updated.getUsername());
		assertEquals(original.getId(), updated.getId());
	}

	@Test
	void shouldReturnNewInstance_whenPasswordChanged() {
		LoginEntity original = LoginEntity.builder()
			.id(UUID.randomUUID())
			.personRef(UUID.randomUUID())
			.username("nick579a")
			.password("Password5!")
			.status(LoginStatus.ACTIVATED)
			.build();

		LoginEntity updated = original.withPassword("NewPassword5!");

		assertNotSame(original, updated);
		assertEquals("NewPassword5!", updated.getPassword());
		assertEquals(original.getId(), updated.getId());
	}

	@Test
	void shouldReturnNewInstance_whenStatusChanged() {
		LoginEntity original = LoginEntity.builder()
			.id(UUID.randomUUID())
			.personRef(UUID.randomUUID())
			.username("nick579a")
			.password("Password5!")
			.status(LoginStatus.ACTIVATED)
			.build();

		LoginEntity updated = original.withStatus(LoginStatus.DEACTIVATED);

		assertNotSame(original, updated);
		assertEquals(LoginStatus.DEACTIVATED, updated.getStatus());
		assertEquals(original.getId(), updated.getId());
	}

	
	@Test
	void shouldThrowException_whenIdIsNull() {
		assertThrows(ValidationException.class, () -> 
			LoginEntity.builder()
				.id(null)
				.personRef(UUID.randomUUID())
				.username("nick579a")
				.password("Password5!")
				.status(LoginStatus.ACTIVATED)
				.build()
		);
	}

	@Test
	void shouldThrowException_whenPersonRefIsNull() {
		assertThrows(ValidationException.class, () -> 
			LoginEntity.builder()
				.id(UUID.randomUUID())
				.personRef(null)
				.username("nick579a")
				.password("Password5!")
				.status(LoginStatus.ACTIVATED)
				.build()
		);
	}

	@Test
	void shouldThrowException_whenUsernameIsNull() {
		assertThrows(ValidationException.class, () -> 
			LoginEntity.builder()
				.id(UUID.randomUUID())
				.personRef(UUID.randomUUID())
				.username(null)
				.password("Password5!")
				.status(LoginStatus.ACTIVATED)
				.build()
		);
	}

	@Test
	void shouldThrowException_whenPasswordIsNull() {
		assertThrows(ValidationException.class, () -> 
			LoginEntity.builder()
				.id(UUID.randomUUID())
				.personRef(UUID.randomUUID())
				.username("nick579a")
				.password(null)
				.status(LoginStatus.ACTIVATED)
				.build()
		);
	}

	@Test
	void shouldThrowException_whenStatusIsNull() {
		assertThrows(ValidationException.class, () -> 
			LoginEntity.builder()
				.id(UUID.randomUUID())
				.personRef(UUID.randomUUID())
				.username("nick579a")
				.password("Password5!")
				.status(null)
				.build()
		);
	}
}
