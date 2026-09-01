package local.sop.datawarehouse.sharedlib.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public class ConsentTypeTest {

    @Test
    void testConsentTypeValues() {
        ConsentType[] values = ConsentType.values();
        assertEquals(3, values.length);
    }

    @Test
    void parse_ShouldReturnStatus_WhenValidInputProvided() throws ValidationException {
        assertEquals(ConsentType.REQUIRED, ConsentType.parse("REQUIRED"));
        assertEquals(ConsentType.REQUIRED, ConsentType.parse("required"));
        assertEquals(ConsentType.OPTIONAL, ConsentType.parse("OPTIONAL"));
        assertEquals(ConsentType.ONE_TIME, ConsentType.parse("ONE_TIME"));
    }

    @Test
    void parse_ShouldThrowValidationException_WhenInputIsNull() {
        ValidationException exception = assertThrows(ValidationException.class, () -> ConsentType.parse(null));
        assertEquals("consent.type.invalid", exception.getMessage());
        assertEquals("type", exception.args().get("field"));
    }

    @Test
    void parse_ShouldThrowValidationException_WhenInputIsInvalid() {
        ValidationException exception = assertThrows(ValidationException.class, () -> ConsentType.parse("INVALID"));
        assertEquals("consent.type.invalid", exception.getMessage());
        assertEquals("type", exception.args().get("field"));
    }
}
