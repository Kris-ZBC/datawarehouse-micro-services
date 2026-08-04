package local.sop.sopinfo.login.domain.model.valueobjects;

import java.util.Map;
import java.util.regex.Pattern;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public record PlainPassword(String value) {
	private static final Pattern HAS_UPPERCASE = Pattern.compile(".*[A-Z].*");
	private static final Pattern HAS_LOWERCASE = Pattern.compile(".*[a-z].*");
	private static final Pattern HAS_DIGIT = Pattern.compile(".*[0-9].*");
	private static final Pattern HAS_SPECIAL_CHAR = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"); // If changed, remember to change it for tests aswell

	private static final int MIN_LENGTH = 8;
	private static final int MAX_LENGTH = 128;

	public PlainPassword {
		if (value == null || value.isBlank() || value.length() < MIN_LENGTH) {
			throw new ValidationException("login.password.short", Map.of("field", "password"));
		}

		if (value.length() > MAX_LENGTH) {
			throw new ValidationException("login.password.long", Map.of("field", "password"));
		}

		if (!HAS_UPPERCASE.matcher(value).matches()) {
			throw new ValidationException("login.password.no.uppercase", Map.of("field", "password"));
		}

		if (!HAS_LOWERCASE.matcher(value).matches()) {
			throw new ValidationException("login.password.no.lowercase", Map.of("field", "password"));
		}

		if (!HAS_DIGIT.matcher(value).matches()) {
			throw new ValidationException("login.password.no.digit", Map.of("field", "password"));
		}

		if (!HAS_SPECIAL_CHAR.matcher(value).matches()) {
			throw new ValidationException("login.password.no.special.char", Map.of("field", "password"));
		}
	}

	public static PlainPassword of(String password) {
		return new PlainPassword(password);
	}
}
