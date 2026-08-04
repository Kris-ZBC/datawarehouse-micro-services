package local.sop.sopinfo.sopinstructor.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.valueobjects.utils.UUIDUtil;

public class SopRefTest {

    private static final String VALID_UUID = UUID.randomUUID().toString();

    @Test
    void shouldCreate_withNewId() {
        SopRef ref = SopRef.newId();
        assertNotNull(ref);
        assertNotNull(ref.value());
    }

    @Test
    void shouldCreate_withOfString() {
        SopRef ref = SopRef.of(VALID_UUID, "value");
        assertEquals(VALID_UUID, ref.value().toString());
    }

    @Test
    void shouldCreate_withOfUUID() {
        UUID uuid = UUIDUtil.parseRequired(VALID_UUID, "value");
        SopRef ref = SopRef.of(uuid);
        assertEquals(VALID_UUID, ref.value().toString());
    }

    @Test
    void shouldCreate_withFromString() {
        SopRef ref = SopRef.fromString(VALID_UUID, "value");
        assertEquals(VALID_UUID, ref.value().toString());
    }

    @Test
    void shouldBeEqual_forSameUUID() {
        SopRef ref1 = SopRef.of(VALID_UUID, "value");
        SopRef ref2 = SopRef.of(VALID_UUID, "value");
        assertEquals(ref1, ref2);
    }

    @Test
    void shouldThrow_whenValueIsNull() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> new SopRef(null));
        assertEquals("key.required", ex.getMessage());
    }

    @Test
    void shouldThrow_whenStringIsInvalidUUID() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> SopRef.of("not-a-uuid", "value"));
        assertEquals("key.invalid", ex.getMessage());
    }

    @Test
    void shouldThrow_whenStringIsNull() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> SopRef.of((String) null, "value"));
        assertEquals("key.required", ex.getMessage());
    }

}
