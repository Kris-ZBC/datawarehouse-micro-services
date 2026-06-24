package local.sop.sopinfo.sharedkernel.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public class LoginStatusTest {

    @Test
    void testLoginStatusValues() {
        LoginStatus[] values = LoginStatus.values();
        assertEquals(2, values.length);
    }

    @Test
    void parse_ShouldReturnStatus_WhenValidInputProvided() throws ValidationException {
        assertEquals(LoginStatus.ACTIVATED, LoginStatus.parse("ACTIVATED"));
        assertEquals(LoginStatus.DEACTIVATED, LoginStatus.parse("DEACTIVATED"));
    }

    @Test
    void parse_ShouldThrowValidationException_WhenInputIsNull() {
        ValidationException exception = assertThrows(ValidationException.class, () -> LoginStatus.parse(null));
        assertEquals("login.status.invalid", exception.getMessage());
        assertEquals("login-status", exception.args().get("field"));
    }

    @Test
    void parse_ShouldThrowValidationException_WhenInputIsInvalid() {
        ValidationException exception = assertThrows(ValidationException.class, () -> LoginStatus.parse("INVALID"));
        assertEquals("login.status.invalid", exception.getMessage());
        assertEquals("login-status", exception.args().get("field"));
    }

    @Test
    void parse_ShouldThrowValidationException_WhenInputIsWrongCase() {
        // LoginStatus.parse uses valueOf(status) directly, which is case-sensitive
        assertThrows(ValidationException.class, () -> LoginStatus.parse("activated"));
    }
}
