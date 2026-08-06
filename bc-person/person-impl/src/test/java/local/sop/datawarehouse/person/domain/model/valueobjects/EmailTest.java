package local.sop.datawarehouse.person.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

class EmailTest {

    @Test
    void shouldCreateEmailWhenValueIsValid() {
        Email email = new Email("test@example.com");

        assertEquals("test@example.com", email.value());
    }

    @Test
    void shouldTrimAndLowercaseEmailWhenCreating() {
        Email email = new Email("  TEST@Example.COM  ");

        assertEquals("test@example.com", email.value());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsNull() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> new Email(null)
        );

        assertEquals("person.email.blank", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsBlank() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> new Email("   ")
        );

        assertEquals("person.email.blank", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenEmailIsInvalid() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> new Email("not-an-email")
        );

        assertEquals("person.email.invalid", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenEmailIsTooLong() {
        String tooLong = "a".repeat(250) + "@x.com";

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> new Email(tooLong)
        );

        assertEquals("person.email.maxLength", exception.getMessage());
    }
}