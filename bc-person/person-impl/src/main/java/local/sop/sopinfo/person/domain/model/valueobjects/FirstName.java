package local.sop.sopinfo.person.domain.model.valueobjects;

import java.util.Map;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public record FirstName(String value) {
    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 100;

    public FirstName {
        if (value == null || value.isBlank()) {
            throw new ValidationException("person.firstname.blank", Map.of("field", "firstName"));
        }

        value = value.trim();

        if (value.length() < MIN_LENGTH) {
            throw new ValidationException("person.firstname.minLength", Map.of(
                "field", "firstName",
                "min", MIN_LENGTH
            ));
        }

        if (value.length() > MAX_LENGTH) {
            throw new ValidationException("person.firstname.maxLength", Map.of(
                "field", "firstName",
                "max", MAX_LENGTH
            ));
        }
    }
}
