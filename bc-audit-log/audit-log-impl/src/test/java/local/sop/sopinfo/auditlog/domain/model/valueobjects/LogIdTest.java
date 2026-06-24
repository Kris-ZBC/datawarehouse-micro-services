package local.sop.sopinfo.auditlog.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

class LogIdTest {

    @Test
    void newId_shouldCreateValidLogId() {
        LogId logId = LogId.newId();

        assertNotNull(logId);
        assertNotNull(logId.value());
    }

    @Test
    void constructor_shouldSetValue_whenValid() {
        UUID uuid = UUID.randomUUID();

        LogId logId = new LogId(uuid);

        assertEquals(uuid, logId.value());
    }

    @Test
    void constructor_shouldThrowValidationException_whenNull() {
        assertThrows(ValidationException.class, () -> new LogId(null));
    }

    @Test
    void value_shouldReturnUnderlyingUuid() {
        UUID uuid = UUID.randomUUID();
        LogId logId = new LogId(uuid);

        assertEquals(uuid, logId.value());
    }

    @Test
    void asString_shouldReturnStringRepresentation() {
        UUID uuid = UUID.randomUUID();
        LogId logId = new LogId(uuid);

        assertEquals(uuid.toString(), logId.asString());
    }

    @Test
    void parse_shouldCreateLogId_whenValidString() {
        UUID uuid = UUID.randomUUID();

        LogId logId = LogId.parse(uuid.toString());

        assertEquals(uuid, logId.value());
    }

    @Test
    void parse_shouldThrowValidationException_whenInvalidString() {
        assertThrows(ValidationException.class, () -> LogId.parse("invalid-uuid"));
    }

    @Test
    void missingException_shouldReturnCorrectException() {
        LogId logId = new LogId(UUID.randomUUID());

        ValidationException ex = logId.missingException();

        assertEquals("log.id.required", ex.getMessage());
        // assertTrue(ex.getDetails().containsKey("field"));
        // assertEquals("log.id", ex.getDetails().get("field"));
    }
}