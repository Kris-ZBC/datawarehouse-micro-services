package local.sop.sopinfo.sopinstructor.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.sharedkernel.valueobjects.utils.UUIDUtil;

public class InstructorRefTest {

    /* happypath */
    @Test
    void should_create_instructorRef_with_valid_uuid() {
        String validUuid = UUID.randomUUID().toString();
        InstructorRef ref = new InstructorRef(UUIDUtil.parseRequired(validUuid, "value"));
        assertEquals(validUuid, ref.value().toString());
    }

    @Test
    void should_create_instructorRef_with_newId() {
        InstructorRef ref = InstructorRef.newId();
        assertEquals(36, ref.value().toString().length()); // UUID string length
    }

    @Test
    void should_create_instructorRef_with_of_string() {
        String validUuid = UUID.randomUUID().toString();;
        InstructorRef ref = InstructorRef.of(validUuid, "value");
        assertEquals(validUuid, ref.value().toString());
    }

    @Test
    void should_create_instructorRef_with_of_uuid() {
        String validUuid = UUID.randomUUID().toString();;
        InstructorRef ref = InstructorRef.of(UUIDUtil.parseRequired(validUuid, "value"));
        assertEquals(validUuid, ref.value().toString());
    }   

    @Test
    void should_create_instructorRef_with_fromString() {
        String validUuid = UUID.randomUUID().toString();
        InstructorRef ref = InstructorRef.fromString(validUuid, "value");
        assertEquals(validUuid, ref.value().toString());
    }

    @Test
    void should_be_equal_for_same_uuid() {
        String validUuid = UUID.randomUUID().toString();
        InstructorRef ref1 = new InstructorRef(UUIDUtil.parseRequired(validUuid, "value"));
        InstructorRef ref2 = new InstructorRef(UUIDUtil.parseRequired(validUuid, "value"));
        assertEquals(ref1, ref2);
    }   

    /* unhappy path */

    @Test
    void should_throw_exception_when_value_is_null() {
        ValidationException exception = assertThrows(ValidationException.class, () -> new InstructorRef(null));
        assertEquals("key.required", exception.getMessage());
    }

    @Test
    void should_throw_exception_when_of_string_with_invalid_uuid() {
        String invalidUuid = "invalid-uuid";
        ValidationException exception = assertThrows(ValidationException.class, () -> InstructorRef.of(invalidUuid, "value"));
        assertEquals("key.invalid", exception.getMessage());
    }

    @Test
    void should_throw_exception_when_of_uuid_with_null() {
        ValidationException exception = assertThrows(ValidationException.class, () -> InstructorRef.of((String) null, "value"));
        assertEquals("key.required", exception.getMessage());
    }

}