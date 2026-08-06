package local.sop.datawarehouse.person.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.enums.PhoneUserType;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.person.domain.model.valueobjects.PhoneNumberId;
import local.sop.datawarehouse.person.domain.model.valueobjects.PhoneNumberValue;

class PhoneNumberTest {

    @Test
    void shouldBuildPhoneNumberWhenAllRequiredValuesAreProvided() {
        PhoneNumberId id = PhoneNumberId.newId();

        PhoneNumber phoneNumber = PhoneNumber.builder()
                .id(id)
                .type(PhoneUserType.SELF)
                .value(new PhoneNumberValue("12345678"))
                .build();

        assertNotNull(phoneNumber);
        assertEquals(id, phoneNumber.getId());
        assertEquals(PhoneUserType.SELF, phoneNumber.getType());
        assertEquals("12345678", phoneNumber.getValue().value());
    }

    @Test
    void shouldGenerateIdWhenIdIsNotProvided() {
        PhoneNumber phoneNumber = PhoneNumber.builder()
                .type(PhoneUserType.SELF)
                .value(new PhoneNumberValue("12345678"))
                .build();

        assertNotNull(phoneNumber.getId());
        assertNotNull(phoneNumber.getId().value());
    }

    @Test
    void toString_shouldContainAllFields() {
        PhoneNumber phoneNumber = PhoneNumber.builder()
                .type(PhoneUserType.SELF)
                .value(new PhoneNumberValue("12345678"))
                .build();

        String result = phoneNumber.toString();

        assertTrue(result.contains("SELF"));
        assertTrue(result.contains("12345678"));
    }

    @Test
    void shouldReturnNewPhoneNumberWhenTypeIsChanged() {
        PhoneNumber original = PhoneNumber.builder()
                .type(PhoneUserType.SELF)
                .value(new PhoneNumberValue("12345678"))
                .build();

        PhoneNumber updated = original.withType(PhoneUserType.PARENT);

        assertNotSame(original, updated);
        assertEquals(PhoneUserType.SELF, original.getType());
        assertEquals(PhoneUserType.PARENT, updated.getType());
        assertEquals(original.getId(), updated.getId());
        assertEquals(original.getValue(), updated.getValue());
    }

    @Test
    void shouldReturnNewPhoneNumberWhenValueIsChanged() {
        PhoneNumber original = PhoneNumber.builder()
                .type(PhoneUserType.SELF)
                .value(new PhoneNumberValue("12345678"))
                .build();

        PhoneNumber updated = original.withValue(new PhoneNumberValue("87654321"));

        assertNotSame(original, updated);
        assertEquals("12345678", original.getValue().value());
        assertEquals("87654321", updated.getValue().value());
        assertEquals(original.getId(), updated.getId());
        assertEquals(original.getType(), updated.getType());
    }

    @Test
    void shouldThrowValidationExceptionWhenTypeIsMissing() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> PhoneNumber.builder()
                        .value(new PhoneNumberValue("12345678"))
                        .build()
        );

        assertEquals("phoneNumber.type.blank", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsMissing() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> PhoneNumber.builder()
                        .type(PhoneUserType.SELF)
                        .build()
        );

        assertEquals("phoneNumber.value.blank", exception.getMessage());
    }
}