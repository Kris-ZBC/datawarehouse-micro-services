package local.sop.datawarehouse.educationline.domain.model.valueobjects;

import java.util.Map;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public record EducationLineName(String value) {
	private static final int MAX_LENGTH = 100;
	private static final int MIN_LENGTH = 1;

	public EducationLineName {
		if (value == null) {
			throw new ValidationException("educationline.name.required", Map.of("field", "educationLineName"));
		}

		value = value.trim();

		if (value.isBlank()) {
			throw new ValidationException("educationline.name.required", Map.of("field", "educationLineName"));
		}

		if (value.chars().anyMatch(Character::isDigit)) {
			throw new ValidationException("educationline.name.contains.number", Map.of("field", "educationLineName"));
		}

		if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
			throw new ValidationException("educationline.name.length.invalid", Map.of("field", "educationLineName"));
		}
	}

	public String value() {
		return value;
	}
}