package local.sop.sopinfo.person.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

class LastNameTest {

    @Test
    void shouldCreateLastNameWhenValueIsValid() {
        LastName lastName = new LastName("Jensen");

        assertEquals("Jensen", lastName.value());
    }

    @Test
    void shouldTrimValueWhenCreatingLastName() {
        LastName lastName = new LastName("  Jensen  ");

        assertEquals("Jensen", lastName.value());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsNull() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> new LastName(null)
        );

        assertEquals("person.lastname.blank", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsBlank() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> new LastName("   ")
        );

        assertEquals("person.lastname.blank", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsTooShort() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> new LastName("J")
        );

        assertEquals("person.lastname.minLength", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsTooLong() {
        String tooLong = "a".repeat(101);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> new LastName(tooLong)
        );

        assertEquals("person.lastname.maxLength", exception.getMessage());
    }
}