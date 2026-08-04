package local.sop.sopinfo.person.domain.model.valueobjects;

import java.util.Map;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;

public record PhoneNumberId(UUID value) implements DomainId {

    public PhoneNumberId {
        if (value == null) {
            throw new ValidationException("key.invalid", Map.of("field", "phoneNumberId"));
        }
    }

    public static PhoneNumberId newId() {
        return new PhoneNumberId(UUID.randomUUID());
    }

    public static PhoneNumberId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("key.invalid", Map.of("field", "phoneNumberId"));
        }

        try {
            return new PhoneNumberId(UUID.fromString(value.trim()));
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("key.invalid", Map.of("field", "phoneNumberId"));
        }
    }
}