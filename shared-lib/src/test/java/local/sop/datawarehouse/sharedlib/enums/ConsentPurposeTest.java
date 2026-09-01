package local.sop.datawarehouse.sharedlib.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public class ConsentPurposeTest {

    @Test
    void testConsentPurposeValues() {
        ConsentPurpose[] values = ConsentPurpose.values();
        assertEquals(7, values.length);
    }

    @ParameterizedTest
	@CsvSource({
		"MARKETING, MARKETING",
		"marketing, MARKETING",
		"ANALYTICS, ANALYTICS",
		"PERSONALIZATION, PERSONALIZATION",
		"THIRD_PARTY_SHARING, THIRD_PARTY_SHARING",
		"REQUIRED_SERVICE, REQUIRED_SERVICE",
		"COMMUNICATION, COMMUNICATION",
		"RESEARCH, RESEARCH"
	})
    void parse_ShouldReturnStatus_WhenValidInputProvided(String input, ConsentPurpose expected) throws ValidationException {
        assertEquals(expected, ConsentPurpose.parse(input));
    }

    @Test
    void parse_ShouldThrowValidationException_WhenInputIsNull() {
        ValidationException exception = assertThrows(ValidationException.class, () -> ConsentPurpose.parse(null));
        assertEquals("consent.purpose.invalid", exception.getMessage());
        assertEquals("purpose", exception.args().get("field"));
    }

    @Test
    void parse_ShouldThrowValidationException_WhenInputIsInvalid() {
        ValidationException exception = assertThrows(ValidationException.class, () -> ConsentPurpose.parse("INVALID"));
        assertEquals("consent.purpose.invalid", exception.getMessage());
        assertEquals("purpose", exception.args().get("field"));
    }
}
