package local.sop.sopinfo.login.domain.model.valueobjects;

import java.time.LocalDateTime;
import java.util.Map;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public record ExpiresAtTimestamp(LocalDateTime value) {
	public ExpiresAtTimestamp {
		if (value == null) {
			throw new ValidationException("login.expiresattimestamp.required", Map.of("field", "expiresAtTimestamp"));
		}

		if (!value.isAfter(LocalDateTime.now())) {
			throw new ValidationException("login.expiresattimestamp.must.be.in.future", Map.of("field", "expiresAtTimestamp"));
		}
	}

	public static ExpiresAtTimestamp of(LocalDateTime instant) {
		return new ExpiresAtTimestamp(instant);
	}
}
