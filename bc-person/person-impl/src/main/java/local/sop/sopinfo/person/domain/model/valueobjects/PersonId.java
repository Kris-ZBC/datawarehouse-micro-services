package local.sop.sopinfo.person.domain.model.valueobjects;

import java.util.Map;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;

public record PersonId(UUID value) implements DomainId {

    public PersonId {
        if (value == null) {
            throw new ValidationException("key.invalid", Map.of("field", "personId"));
        }
    }

	public static PersonId newId() {
		return new PersonId(UUID.randomUUID());
	}

        public static PersonId of(UUID value) {
        return new PersonId(value);
    }

    public static PersonId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("key.invalid", Map.of("field", "personId"));
        }

        try {
            return new PersonId(UUID.fromString(value.trim()));
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("key.invalid", Map.of("field", "personId"));
        }
    }


}