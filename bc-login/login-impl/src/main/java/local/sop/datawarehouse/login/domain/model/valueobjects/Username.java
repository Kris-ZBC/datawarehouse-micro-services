package local.sop.datawarehouse.login.domain.model.valueobjects;

import java.util.Map;
import java.util.regex.Pattern;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public record Username(String value) {
	private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+@zbc\\.dk$");

	public Username {
		if (value == null || !USERNAME_PATTERN.matcher(value).matches()) { 
			throw new ValidationException("login.username.invalid", Map.of("field", "username"));
		}
	}

	public static Username of(String username) {
		return new Username(username);
	}
}
