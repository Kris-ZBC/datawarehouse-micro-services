package local.sop.sopinfo.person.domain.model.valueobjects;

import java.util.Map;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public record LastName(String value) {

	private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 100;

	public LastName{
		if (value == null || value.isBlank()){
			throw new ValidationException("person.lastname.blank", Map.of("field", "lastName"));
		}

		value = value.trim();

		if (value.length() < MIN_LENGTH){
			throw new ValidationException("person.lastname.minLength", Map.of("field", "lastName", "min", MIN_LENGTH));
		}
		
		if (value.length() > MAX_LENGTH){
			throw new ValidationException("person.lastname.maxLength", Map.of("field", "lastName", "max", MAX_LENGTH));
		}
	}
}
