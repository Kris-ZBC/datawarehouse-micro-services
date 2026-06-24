package local.sop.sopinfo.sopinstructor.interfaceadapters.persistance.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.sopinstructor.interfaceadapters.persistence.jpa.SopInstructorEntity;
import local.sop.sopinfo.sopinstructor.interfaceadapters.persistence.jpa.SopInstructorId;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

class SopInstructorEntityTest {

    @Test
    void builder_createsEntity_andGettersWork() {
        UUID sopRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();

        SopInstructorId id = new SopInstructorId(sopRef, instructorRef);

        SopInstructorEntity entity = SopInstructorEntity.builder()
                .id(id)
                .active(true)
                .build();

        assertEquals(sopRef, entity.getId().getSopRef());
        assertEquals(instructorRef, entity.getId().getInstructorRef());
        assertTrue(entity.isActive());
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    void withActive_updatesField() {
        SopInstructorId id = new SopInstructorId(UUID.randomUUID(), UUID.randomUUID());

        SopInstructorEntity entity = SopInstructorEntity.builder()
                .id(id)
                .active(true)
                .build();

        entity.withActive(false);

        assertFalse(entity.isActive());
    }

    @Test
    void builder_throws_whenIdMissing() {
        var ex =assertThrows(ValidationException.class, () ->
                SopInstructorEntity.builder()
                        .active(true)
                        .build()
        );
        assertEquals("key.required", ex.getMessage());
    }

    @Test
    void builder_throws_whenActiveMissing() {
        SopInstructorId id = new SopInstructorId(UUID.randomUUID(), UUID.randomUUID());

        var ex = assertThrows(ValidationException.class, () ->
                SopInstructorEntity.builder()
                        .id(id)
                        .build()
        );
        assertEquals("sop-instructor.active.required", ex.getMessage());
    }
}