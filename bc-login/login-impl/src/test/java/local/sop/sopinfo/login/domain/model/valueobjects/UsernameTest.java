package local.sop.sopinfo.login.domain.model.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class UsernameTest {

	@ParameterizedTest
	@ValueSource(strings = {"john@zbc.dk", "john.doe@zbc.dk", "john_doe@zbc.dk", "john-doe@zbc.dk", "j@zbc.dk"})
	void shouldCreateUsername_whenValidZbcEmail(String input) {
		Username username = new Username(input);
		assertEquals(input, username.value());
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"john@zbc.dk.extra",   // characters after domain
		"john@zbcXdk",         // wrong domain
		"john@zbc.dk2",        // trailing character
		"john doe@zbc.dk",     // space before @
		"john#doe@zbc.dk",     // special char before @
		"john!@zbc.dk",        // special char before @
		"@zbc.dk",             // empty local part
		"zbc.dk",              // missing @
		"john@",               // missing domain
		"",                    // blank
	})
	void shouldThrowException_whenInvalidUsername(String input) {
		assertThrows(ValidationException.class, () -> new Username(input));
	}

	@Test
	void shouldThrowException_whenNull() {
		assertThrows(ValidationException.class, () -> new Username(null));
	}
}
