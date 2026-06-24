package local.sop.sopinfo.auditlog.domain.model.valueobjects;

import java.util.Map;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public final class OriginSystem {

	private final String value;

	public OriginSystem(String value) {
		if (value == null || value.isBlank()) {
			throw new ValidationException("log.origin.system.required", Map.of("field", "originSystem"));
		}
		this.value = value;
	}

	public String value() {
		return value;
	}
	
	public static OriginSystem newOriginSystem(String input) {
		if (input == null || input.isBlank()) {
			throw new ValidationException("log.origin.system.required", Map.of("field", "originSystem"));
		}
        return new OriginSystem(input);
    }

	@Override
	public String toString() {
		return value;
	}
}