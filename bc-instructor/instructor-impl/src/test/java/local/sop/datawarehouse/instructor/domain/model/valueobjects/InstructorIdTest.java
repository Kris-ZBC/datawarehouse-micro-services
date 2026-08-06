package local.sop.datawarehouse.instructor.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

class InstructorIdTest {

    @Test
    void newId_shouldCreateValidInstructorId() {
        InstructorId id = InstructorId.newId();

        assertNotNull(id);
        assertNotNull(id.value());
    }

    @Test
    void constructor_shouldSetValue_whenValid() {
        UUID uuid = UUID.randomUUID();

        InstructorId id = new InstructorId(uuid);

        assertEquals(uuid, id.value());
    }

    @Test
    void constructor_shouldThrowValidationException_whenNull() {
        assertThrows(ValidationException.class, () -> new InstructorId(null));
    }

    @Test
    void of_shouldCreateInstructorId_whenValidUuid() {
        UUID uuid = UUID.randomUUID();

        InstructorId id = InstructorId.of(uuid);

        assertEquals(uuid, id.value());
    }

    @Test
    void fromString_shouldCreateInstructorId_whenValidString() {
        UUID uuid = UUID.randomUUID();

        InstructorId id = InstructorId.fromString(uuid.toString());

        assertEquals(uuid, id.value());
    }

    @Test
    void fromString_shouldThrowValidationException_whenInvalidString() {
        assertThrows(ValidationException.class, () -> InstructorId.fromString("not-a-uuid"));
    }

    @Test
    void asString_shouldReturnUuidAsString() {
        UUID uuid = UUID.randomUUID();

        InstructorId id = new InstructorId(uuid);

        assertEquals(uuid.toString(), id.asString());
    }
}