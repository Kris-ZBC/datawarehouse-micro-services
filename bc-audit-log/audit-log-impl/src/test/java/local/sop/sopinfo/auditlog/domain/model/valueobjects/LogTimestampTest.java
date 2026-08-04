package local.sop.sopinfo.auditlog.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

class LogTimestampTest {

    @Test
    void constructor_shouldSetValue_whenValid() {
        Instant now = Instant.now();

        LogTimestamp timestamp = new LogTimestamp(now);

        assertEquals(now, timestamp.value());
    }

    @Test
    void constructor_shouldThrowValidationException_whenNull() {
        assertThrows(ValidationException.class, () -> new LogTimestamp(null));
    }

    @Test
    void now_shouldReturnCurrentTimestamp() {
        Instant before = Instant.now();

        LogTimestamp timestamp = LogTimestamp.now();

        Instant after = Instant.now();

        assertNotNull(timestamp);
        assertNotNull(timestamp.value());

        // Ensure the timestamp is within the expected range
        assertFalse(timestamp.value().isBefore(before));
        assertFalse(timestamp.value().isAfter(after));
    }
}