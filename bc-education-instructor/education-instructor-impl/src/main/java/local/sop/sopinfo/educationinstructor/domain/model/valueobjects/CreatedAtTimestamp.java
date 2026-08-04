package local.sop.sopinfo.educationinstructor.domain.model.valueobjects;

import java.time.LocalDateTime;
import java.util.Map;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public record CreatedAtTimestamp(LocalDateTime value) {
	public CreatedAtTimestamp {
		if (value == null) {
			throw new ValidationException("educationinstructor.createdat.required", Map.of("field", "createdAt", "createdAt", value==null? "null" : value.toString()));
		}
	}

}
