package local.sop.sopinfo.sharedkernel.valueobjects.utils;

import java.util.Map;
import java.util.UUID;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public final class UUIDUtil {
    private UUIDUtil() {}

    public static UUID parseRequired(String raw, String field) {
        if (raw == null || raw.isBlank())
            throw new ValidationException("key.required", Map.of("field", field));
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("key.invalid", Map.of("field", field, "value", raw));
        }
    }

    /** Require non-null UUID value (used by VO compact constructors). */
    public static UUID require(UUID id, String field) {
        if (id == null)
            throw new ValidationException("key.required", Map.of("field", field));
        return id;
    }

    /** Generate a new random UUID. */
    public static UUID newUuid() {
        return UUID.randomUUID();
    }


}
