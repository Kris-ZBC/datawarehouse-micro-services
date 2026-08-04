package local.sop.sopinfo.messageperson.domain.model.valueobjects;

import java.time.LocalDateTime;
import java.util.Map;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public record CreatedAtTimestamp(LocalDateTime value) {
	public CreatedAtTimestamp {
		if (value == null) {
			throw new ValidationException("message-person.createdate.required", Map.of("field", "createdAt", "createdAt", value==null? "null" : value.toString()));
		}

		LocalDateTime now = LocalDateTime.now();
		if (value.isAfter(now)) {
			throw new ValidationException(("message-person.createdate.isafter"), Map.of("createdAt",value));
		}
	}

}
