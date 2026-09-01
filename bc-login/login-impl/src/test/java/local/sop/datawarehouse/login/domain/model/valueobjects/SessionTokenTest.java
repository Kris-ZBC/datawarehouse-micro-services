package local.sop.datawarehouse.login.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public class SessionTokenTest {
	@Test
	void shouldCreateSessionToken_whenValueIsValid() {
		String uuid = UUID.randomUUID().toString();
		SessionToken sessionToken = new SessionToken(uuid);
		assertEquals(uuid, sessionToken.value());
	}

	@Test
	void shouldThrowException_whenValueIsNull() {
		assertThrows(ValidationException.class, () -> new SessionToken(null));
	}

	@Test
	void shouldThrowException_whenValueIsBlank() {
		assertThrows(ValidationException.class, () -> new SessionToken("   "));
	}

	@Test
	void shouldThrowException_whenValueIsTooShort() {
		assertThrows(ValidationException.class, () -> new SessionToken("short"));
	}
}
