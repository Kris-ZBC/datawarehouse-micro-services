package local.sop.datawarehouse.login.domain.model.valueobjects;

import java.time.LocalDateTime;
import java.util.Map;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public record CreatedAtTimestamp(LocalDateTime value) {
	public CreatedAtTimestamp {
		if (value == null) {
			throw new ValidationException("login.createdattimestamp.required", Map.of("field", "createdAtTimestamp"));
		}

		if (value.isAfter(LocalDateTime.now())) {
			throw new ValidationException("login.createdattimestamp.not.in.future", Map.of("field", "createdAtTimestamp"));
		}
	}

	public static CreatedAtTimestamp of(LocalDateTime value) {
		return new CreatedAtTimestamp(value);
	}
}
