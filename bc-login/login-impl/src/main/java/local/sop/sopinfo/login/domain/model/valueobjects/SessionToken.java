package local.sop.sopinfo.login.domain.model.valueobjects;

import java.util.Map;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public record SessionToken(String value) {
	public SessionToken {
		if (value == null || value.isBlank()) { 
			throw new ValidationException("session.token.invalid", Map.of("field", "token"));
		}

		if(value.length() < 16) {
			throw new ValidationException("session.token.short", Map.of("field", "token"));
		}
	}

	public static SessionToken of(String uuid) {
		return new SessionToken(uuid);
	}
}
