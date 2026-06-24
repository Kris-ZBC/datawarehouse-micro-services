package local.sop.sopinfo.educationline.domain.model.valueobjects;

import java.time.Instant;
import java.util.Map;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public record EducationLineCreatedAt(Instant value) {

	public EducationLineCreatedAt {
		if (value == null) {
			throw new ValidationException("educationline.createdat.required", Map.of("field", "educationLineCreatedAt"));
		}
	}

	public static EducationLineCreatedAt now() {
		return new EducationLineCreatedAt(Instant.now());
	}

	public Instant value() {
		return value;
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
