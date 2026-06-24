package local.sop.sopinfo.education.domain.model.valueobjects;

import java.util.Map;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public record EducationName(String value) {
    private static final int MAX_LENGTH = 100;

    public EducationName {
        if (value == null) {
            throw new ValidationException("education.name.required", Map.of("field", "educationName"));
        }

        value = value.trim();

        if (value.isBlank()) {
            throw new ValidationException("education.name.required", Map.of("field", "educationName"));
        }

        if (value.length() > MAX_LENGTH) {
            throw new ValidationException("education.name.length.invalid", Map.of("field", "educationName"));
        }
    }

    public String value() {
        return value;
    }
}