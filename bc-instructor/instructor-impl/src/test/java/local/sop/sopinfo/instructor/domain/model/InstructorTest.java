package local.sop.sopinfo.instructor.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.instructor.domain.model.valueobjects.InstructorId;
import local.sop.sopinfo.instructor.domain.model.valueobjects.PersonRef;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

class InstructorTest {

    @Test
    void builder_shouldSetFields_whenValid() {
        InstructorId id = InstructorId.of(UUID.randomUUID());
        PersonRef personRef = PersonRef.of(UUID.randomUUID());

        Instructor instructor = Instructor.builder()
                .id(id)
                .personRef(personRef)
                .build();

        assertEquals(id, instructor.getId());
        assertEquals(personRef, instructor.getPersonRef());
    }

    @Test
    void builder_shouldGenerateId_whenIdIsNull() {
        PersonRef personRef = PersonRef.of(UUID.randomUUID());

        Instructor instructor = Instructor.builder()
                .personRef(personRef)
                .build();

        assertNotNull(instructor.getId());
        assertNotNull(instructor.getId().value());
        assertEquals(personRef, instructor.getPersonRef());
    }

    @Test
    void builder_shouldThrowValidationException_whenPersonRefIsNull() {
        assertThrows(ValidationException.class, () ->
                Instructor.builder()
                        .build()
        );
    }

    @Test
    void create_shouldCreateInstructor_whenValid() {
        PersonRef personRef = PersonRef.of(UUID.randomUUID());

        Instructor instructor = Instructor.create(personRef);

        assertNotNull(instructor.getId());
        assertEquals(personRef, instructor.getPersonRef());
    }

    @Test
    void create_shouldThrowValidationException_whenPersonRefIsNull() {
        assertThrows(ValidationException.class, () -> Instructor.create(null));
    }

    @Test
    void withPersonRef_shouldReturnNewInstructorWithUpdatedPersonRef() {
        InstructorId id = InstructorId.of(UUID.randomUUID());
        PersonRef originalPersonRef = PersonRef.of(UUID.randomUUID());
        PersonRef newPersonRef = PersonRef.of(UUID.randomUUID());

        Instructor original = Instructor.builder()
                .id(id)
                .personRef(originalPersonRef)
                .build();

        Instructor updated = original.withPersonRef(newPersonRef);

        assertEquals(id, updated.getId());
        assertEquals(newPersonRef, updated.getPersonRef());
        assertEquals(originalPersonRef, original.getPersonRef());
    }

    @Test
    void withPersonRef_shouldThrowValidationException_whenNull() {
        Instructor instructor = Instructor.create(PersonRef.of(UUID.randomUUID()));

        assertThrows(ValidationException.class, () -> instructor.withPersonRef(null));
    }
}