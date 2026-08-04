package local.sop.sopinfo.login.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public class PasswordTest {

	private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{};':\"\\\\|,.<>/?"; // Special characters which is allowed in the password

	@Test
	void shouldCreatePassword_whenValidInput() {
		String input = "Password1!";
		PlainPassword password = new PlainPassword(input);
		assertEquals(input, password.value());
	}

	private static Stream<Character> allowedSpecialCharacters() {
		return SPECIAL_CHARS
			.chars()
			.mapToObj(c -> (char) c);
	}

	@ParameterizedTest
	@MethodSource("allowedSpecialCharacters")
	void shouldCreatePassword_whenContainsAllowedSpecialCharacters(char specialChar) {
		String input = "Password1" + specialChar;
		PlainPassword password = new PlainPassword(input);
		assertEquals(input, password.value());
	}

	@Test
	void shouldCreatePassword_whenLengthIsExactlyMinimum() {
		String input = "Passwo1!";
		PlainPassword password = new PlainPassword(input);
		assertEquals(input, password.value());
	}

	@Test
	void shouldCreatePassword_whenLengthIsExactlyMaximum() {
		String input = "P" + "a".repeat(125) + "1!";
		PlainPassword password = new PlainPassword(input);
		assertEquals(input, password.value());
	}

	@Test
	void shouldThrowException_whenLengthIsBelowMinimum() {
		String input = "Pass12!";
		assertThrows(ValidationException.class, () -> new PlainPassword(input));
	}

	@Test
	void shouldThrowException_whenLengthExceedsMaximum() {
		String input = "P" + "a".repeat(126) + "1!";
		assertThrows(ValidationException.class, () -> new PlainPassword(input));
	}

	@Test
	void shouldThrowException_whenMissingUppercase() {
		String input = "password1!";
		assertThrows(ValidationException.class, () -> new PlainPassword(input));
	}

	@Test
	void shouldThrowException_whenMissingLowercase() {
		String input = "PASSWORD1!";
		assertThrows(ValidationException.class, () -> new PlainPassword(input));
	}

	@Test
	void shouldThrowException_whenMissingDigit() {
		String input = "Password!";
		assertThrows(ValidationException.class, () -> new PlainPassword(input));
	}

	@Test
	void shouldThrowException_whenMissingSpecialCharacter() {
		String input = "Password1";
		assertThrows(ValidationException.class, () -> new PlainPassword(input));
	}

	@ParameterizedTest
	@ValueSource(strings = {"Password1 ", "Password1\n", "Password1\t"})
	void shouldThrowException_whenContainsInvalidCharacters(String input) {
		assertThrows(ValidationException.class, () -> new PlainPassword(input));
	}

	@Test
	void shouldThrowException_whenNull() {
		String input = null;
		assertThrows(ValidationException.class, () -> new PlainPassword(input));
	}

	@Test
	void shouldThrowException_whenBlank() {
		String input = "";
		assertThrows(ValidationException.class, () -> new PlainPassword(input));
	}

}
