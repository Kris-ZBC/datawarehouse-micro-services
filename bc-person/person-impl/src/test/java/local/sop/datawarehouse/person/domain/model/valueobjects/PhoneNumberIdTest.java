package local.sop.datawarehouse.person.domain.model.valueobjects;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

class PhoneNumberIdTest {

    @Test
    void shouldCreatePhoneNumberIdWhenValueIsValid() {
        UUID uuid = UUID.randomUUID();

        PhoneNumberId phoneNumberId = new PhoneNumberId(uuid);

        assertEquals(uuid, phoneNumberId.value());
    }

    @Test
    void shouldCreatePhoneNumberIdFromStringWhenValueIsValid() {
        UUID uuid = UUID.randomUUID();

        PhoneNumberId phoneNumberId = PhoneNumberId.fromString(uuid.toString());

        assertEquals(uuid, phoneNumberId.value());
    }

    @Test
    void shouldCreateNewPhoneNumberId() {
        PhoneNumberId phoneNumberId = PhoneNumberId.newId();

        assertNotNull(phoneNumberId);
        assertNotNull(phoneNumberId.value());
    }

    @Test
    void shouldCreateUniquePhoneNumberIds() {
        PhoneNumberId first = PhoneNumberId.newId();
        PhoneNumberId second = PhoneNumberId.newId();

        assertNotEquals(first, second);
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsNull() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> new PhoneNumberId(null)
        );

        assertEquals("key.invalid", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsBlank() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> PhoneNumberId.fromString("   ")
        );

        assertEquals("key.invalid", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsInvalid() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> PhoneNumberId.fromString("not-a-uuid")
        );

        assertEquals("key.invalid", exception.getMessage());
    }
}