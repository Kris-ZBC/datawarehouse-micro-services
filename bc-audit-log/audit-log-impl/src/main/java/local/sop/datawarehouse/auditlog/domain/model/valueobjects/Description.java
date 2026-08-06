package local.sop.datawarehouse.auditlog.domain.model.valueobjects;

import java.util.Map;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

/**
 * Optional human-readable description. May be null.
 */
public record Description(String value) {

    public Description {
        // May be null; if present, trim whitespace.
        if (value != null && value.isBlank()) {
            value = null; // normalize blank to null
        }
    }

    public static Description newDescription(String input) {
		if (input == null || input.isBlank()) {
			throw new ValidationException("log.description.required", Map.of("field", "description"));
		}
        return new Description(input);
    }

    @Override
    public String toString() {
        return value == null ? "" : value;
    }
}
