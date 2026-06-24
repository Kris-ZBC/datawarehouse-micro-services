package local.sop.sopinfo.consent.domain.model.valueobject;

import java.util.Map;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public record ConsentStatementValue(String value) {
	private static final int MAX_LENGTH = 1000;
	public ConsentStatementValue {
		if (value == null || value.isBlank()) {
			throw new ValidationException("statementtext.invalid", Map.of("field", "statementText"));
		}
		if (value.length() > MAX_LENGTH) {
			throw new ValidationException("statementtext.length.invalid", Map.of("field", "statementText", "max", MAX_LENGTH));
		}
	}
}