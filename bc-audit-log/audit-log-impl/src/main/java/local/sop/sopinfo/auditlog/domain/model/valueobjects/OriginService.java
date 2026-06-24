package local.sop.sopinfo.auditlog.domain.model.valueobjects;

import java.util.Map;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public final class OriginService {

    private final String value;

    public OriginService(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("log.origin.service.required", Map.of("field", "originService"));
        }
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static OriginService newOriginService(String input) {
		if (input == null || input.isBlank()) {
			throw new ValidationException("log.origin.service.required", Map.of("field", "originService"));
		}
        return new OriginService(input);
    }

    @Override
    public String toString() {
        return value;
    }
}
