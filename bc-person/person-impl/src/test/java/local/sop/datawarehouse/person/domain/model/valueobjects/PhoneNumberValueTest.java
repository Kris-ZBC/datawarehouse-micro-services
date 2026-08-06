package local.sop.datawarehouse.person.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

class PhoneNumberValueTest {

    @Test
    void shouldCreatePhoneNumberWhenValueIsValid() {
        PhoneNumberValue phoneNumberValue = new PhoneNumberValue("1234567");

        assertEquals("1234567", phoneNumberValue.value());
    }

    @Test
    void shouldTrimAndNormalizePhoneNumberWhenValueContainsFormatting() {
        PhoneNumberValue phoneNumberValue = new PhoneNumberValue("  +45 12 34 56 78  ");

        assertEquals("+4512345678", phoneNumberValue.value());
    }

    @Test
    void shouldRemoveNonDigitsWhenPhoneNumberDoesNotStartWithPlus() {
        PhoneNumberValue phoneNumberValue = new PhoneNumberValue("(+45) 12-34-56-78");

        assertEquals("4512345678", phoneNumberValue.value());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsNull() {
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new PhoneNumberValue(null)
        );

        assertEquals("person.phonenumber.blank", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsBlank() {
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new PhoneNumberValue("   ")
        );

        assertEquals("person.phonenumber.blank", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueContainsNoDigits() {
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new PhoneNumberValue("++--()")
        );

        assertEquals("person.phonenumber.digitsRequired", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsTooShort() {
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new PhoneNumberValue("123456")
        );

        assertEquals("person.phonenumber.minLength", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsTooLong() {
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new PhoneNumberValue("1234567890123456")
        );

        assertEquals("person.phonenumber.maxLength", exception.getMessage());
    }
}