package local.sop.datawarehouse.person.domain.model.valueobjects;

import java.util.Map;
import java.util.regex.Pattern;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public record Email(String value) {

	private static final int MAX_LENGTH = 254;

    // Pragmatic email validation:
    // - requires one @
    // - requires something before @
    // - requires a domain with at least one dot
    // - no whitespace
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

	public Email{
		if (value == null || value.isBlank()){
			throw new ValidationException("person.email.blank", Map.of("field", "email"));
		}

		value = value.trim().toLowerCase();

		if (value.length() > MAX_LENGTH) {
			throw new ValidationException("person.email.maxLength", Map.of("field", "email", "max", MAX_LENGTH));
		}

		if (!EMAIL_PATTERN.matcher(value).matches()) {
			throw new ValidationException("person.email.invalid", Map.of("field", "email"));
		}
	}


}
