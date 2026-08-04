package local.sop.sopinfo.auditlog.domain.model.valueobjects;

import java.time.Instant;
import java.util.Map;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

/**
 * Timestamp captured at log creation time.
 */
public record LogTimestamp(Instant value) {

    public LogTimestamp {
        if (value == null) {
            throw new ValidationException("log.timestamp.required", Map.of("field", "timestamp"));
        }
    }

    public static LogTimestamp now() {
        return new LogTimestamp(Instant.now());
    }
}
