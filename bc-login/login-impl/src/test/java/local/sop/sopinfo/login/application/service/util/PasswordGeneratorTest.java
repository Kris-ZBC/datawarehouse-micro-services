package local.sop.sopinfo.login.application.service.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PasswordGeneratorTest {

	private final PasswordGenerator generator = new PasswordGenerator();

	@Test
	void shouldGenerateValidPassword() {
		String password = generator.generate();

		assertTrue(password.matches(".*[A-Z].*"));
		assertTrue(password.matches(".*[a-z].*"));
		assertTrue(password.matches(".*[0-9].*"));
		assertTrue(password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"));
		assertTrue(password.length() >= 8);
	}
}
