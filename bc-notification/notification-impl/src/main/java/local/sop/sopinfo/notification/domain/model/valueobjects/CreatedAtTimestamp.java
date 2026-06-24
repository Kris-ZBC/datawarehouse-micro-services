package local.sop.sopinfo.notification.domain.model.valueobjects;

import java.time.LocalDateTime;
import java.util.Map;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public record CreatedAtTimestamp(LocalDateTime value) {
	public CreatedAtTimestamp {
		if (value == null) {
			throw new ValidationException("notification.createdattimestamp.required", Map.of("field", "createdAttimestamp"));
		}

		LocalDateTime now = LocalDateTime.now();
		if (value.isAfter(now)) {
			throw new ValidationException("notification.createdattimestamp.not.in.future", Map.of("field", "createdAttimestamp"));
		}
	}
}
