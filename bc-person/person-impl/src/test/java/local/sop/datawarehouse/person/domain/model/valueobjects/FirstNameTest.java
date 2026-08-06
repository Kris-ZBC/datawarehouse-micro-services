package local.sop.datawarehouse.person.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

class FirstNameTest {

    @Test
    void shouldCreateFirstNameWhenValueIsValid() {
        FirstName firstName = new FirstName("John");

        assertEquals("John", firstName.value());
    }

    @Test
    void shouldTrimValueWhenCreatingFirstName() {
        FirstName firstName = new FirstName("  John  ");

        assertEquals("John", firstName.value());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsNull() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> new FirstName(null)
        );

        assertEquals("person.firstname.blank", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsBlank() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> new FirstName("   ")
        );

        assertEquals("person.firstname.blank", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsTooShort() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> new FirstName("J")
        );

        assertEquals("person.firstname.minLength", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsTooLong() {
        String tooLong = "a".repeat(101);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> new FirstName(tooLong)
        );

        assertEquals("person.firstname.maxLength", exception.getMessage());
    }
}