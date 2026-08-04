package local.sop.sopinfo.person.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

class PersonIdTest {

    @Test
    void shouldCreatePersonIdWhenValueIsValid() {
        UUID uuid = UUID.randomUUID();

        PersonId personId = new PersonId(uuid);

        assertEquals(uuid, personId.value());
    }

    @Test
    void shouldCreatePersonIdFromStringWhenValueIsValid() {
        UUID uuid = UUID.randomUUID();

        PersonId personId = PersonId.fromString(uuid.toString());

        assertEquals(uuid, personId.value());
    }

    @Test
    void shouldCreateNewPersonId() {
        PersonId personId = PersonId.newId();

        assertNotNull(personId);
        assertNotNull(personId.value());
    }

    @Test
    void shouldCreateUniquePersonIds() {
        PersonId first = PersonId.newId();
        PersonId second = PersonId.newId();

        assertNotEquals(first, second);
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsNull() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> new PersonId(null)
        );

        assertEquals("key.invalid", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsBlank() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> PersonId.fromString("   ")
        );

        assertEquals("key.invalid", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsInvalid() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> PersonId.fromString("not-a-uuid")
        );

        assertEquals("key.invalid", exception.getMessage());
    }
}