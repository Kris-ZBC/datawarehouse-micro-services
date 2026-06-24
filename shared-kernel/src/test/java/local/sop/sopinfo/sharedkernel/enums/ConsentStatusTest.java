package local.sop.sopinfo.sharedkernel.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public class ConsentStatusTest {

    @Test
    void testConsentStatusEnumValues() {
        ConsentStatus[] statuses = ConsentStatus.values();
        assertEquals(3, statuses.length);
    }

    @Test
    void isActive_ShouldReturnTrue_WhenStatusIsActive() {
        assertTrue(ConsentStatus.ACTIVE.isActive());
        assertFalse(ConsentStatus.WITHDRAWN.isActive());
        assertFalse(ConsentStatus.EXPIRED.isActive());
    }

    @Test
    void canBeWithdrawn_ShouldReturnTrue_WhenStatusIsActive() {
        assertTrue(ConsentStatus.ACTIVE.canBeWithdrawn());
        assertFalse(ConsentStatus.WITHDRAWN.canBeWithdrawn());
        assertFalse(ConsentStatus.EXPIRED.canBeWithdrawn());
    }

    @Test
    void parse_ShouldReturnStatus_WhenValidInputProvided() throws ValidationException {
        assertEquals(ConsentStatus.ACTIVE, ConsentStatus.parse("ACTIVE"));
        assertEquals(ConsentStatus.ACTIVE, ConsentStatus.parse("active"));
        assertEquals(ConsentStatus.WITHDRAWN, ConsentStatus.parse("WITHDRAWN"));
        assertEquals(ConsentStatus.EXPIRED, ConsentStatus.parse("EXPIRED"));
    }

    @Test
    void parse_ShouldThrowValidationException_WhenInputIsNull() {
        ValidationException exception = assertThrows(ValidationException.class, () -> ConsentStatus.parse(null));
        assertEquals("consent.status.invalid", exception.getMessage());
        assertEquals("status", exception.args().get("field"));
    }

    @Test
    void parse_ShouldThrowValidationException_WhenInputIsInvalid() {
        ValidationException exception = assertThrows(ValidationException.class, () -> ConsentStatus.parse("INVALID"));
        assertEquals("consent.status.invalid", exception.getMessage());
        assertEquals("status", exception.args().get("field"));
    }
}
