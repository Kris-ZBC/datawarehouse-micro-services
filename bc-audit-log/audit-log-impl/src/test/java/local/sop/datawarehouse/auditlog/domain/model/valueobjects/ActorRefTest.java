package local.sop.datawarehouse.auditlog.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

class ActorRefTest {

    @Test
    void constructor_shouldSetValue() {
        UUID id = UUID.randomUUID();
        ActorRef ref = new ActorRef(id);

        assertEquals(id, ref.value());
    }

    @Test
    void constructor_shouldThrowValidationException_whenValueIsNull() {
        ValidationException ex = assertThrows(ValidationException.class, () -> new ActorRef(null));
        assertEquals("log.actorRef.required", ex.getMessage());
    }

    @Test
    void toString_shouldReturnUuidString() {
        UUID id = UUID.randomUUID();
        ActorRef ref = new ActorRef(id);

        assertEquals(id.toString(), ref.toString());
    }
}