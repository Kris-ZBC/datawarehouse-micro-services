package local.sop.sopinfo.instructor.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

class PersonRefTest {

    @Test
    void constructor_shouldSetValue_whenValid() {
        UUID uuid = UUID.randomUUID();

        PersonRef personRef = new PersonRef(uuid);

        assertEquals(uuid, personRef.value());
    }

    @Test
    void constructor_shouldThrowValidationException_whenNull() {
        assertThrows(ValidationException.class, () -> new PersonRef(null));
    }

    @Test
    void of_shouldCreatePersonRef_whenValidUuid() {
        UUID uuid = UUID.randomUUID();

        PersonRef personRef = PersonRef.of(uuid);

        assertEquals(uuid, personRef.value());
    }

    @Test
    void fromString_shouldCreatePersonRef_whenValidString() {
        UUID uuid = UUID.randomUUID();

        PersonRef personRef = PersonRef.fromString(uuid.toString());

        assertEquals(uuid, personRef.value());
    }

    @Test
    void fromString_shouldThrowValidationException_whenInvalidString() {
        assertThrows(ValidationException.class, () -> PersonRef.fromString("not-a-uuid"));
    }

    @Test
    void asString_shouldReturnUuidAsString() {
        UUID uuid = UUID.randomUUID();

        PersonRef personRef = new PersonRef(uuid);

        assertNotNull(personRef);
    }
}