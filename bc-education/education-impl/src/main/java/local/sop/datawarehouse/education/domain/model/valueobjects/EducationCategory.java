package local.sop.datawarehouse.education.domain.model.valueobjects;

import java.util.Map;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public record EducationCategory(String value) {
    private static final int MAX_LENGTH = 100;

    public EducationCategory {
        if (value == null) {
            throw new ValidationException("education.category.required", Map.of("field", "educationCategory"));
        }

        value = value.trim();

        if (value.isBlank()) {
            throw new ValidationException("education.category.required", Map.of("field", "educationCategory"));
        }

        if (value.length() > MAX_LENGTH) {
            throw new ValidationException("education.category.length.invalid", Map.of("field", "educationCategory"));
        }
    }

    public String value() {
        return value;
    }
}