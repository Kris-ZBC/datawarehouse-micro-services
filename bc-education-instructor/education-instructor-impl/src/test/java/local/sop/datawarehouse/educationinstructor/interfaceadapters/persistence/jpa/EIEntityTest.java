package local.sop.datawarehouse.educationinstructor.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

class EIEntityTest {

    @Test
    void builder_createsEntity_andGettersWork() {
        UUID educationRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();

        EIId id = new EIId(educationRef, instructorRef);

        EIEntity entity = EIEntity.builder()
                .id(id)
                .active(true)
                .build();

        assertEquals(educationRef, entity.getId().getEducationRef());
        assertEquals(instructorRef, entity.getId().getInstructorRef());
        assertTrue(entity.isActive());
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    void withActive_updatesField() {
        EIId id = new EIId(UUID.randomUUID(), UUID.randomUUID());

        EIEntity entity = EIEntity.builder()
                .id(id)
                .active(true)
                .build();

        entity.withActive(false);

        assertFalse(entity.isActive());
    }

    @Test
    void builder_throws_whenIdMissing() {
        var ex =assertThrows(ValidationException.class, () ->
                EIEntity.builder()
                        .active(true)
                        .build()
        );
        assertEquals("key.required", ex.getMessage());
    }

    @Test
    void builder_throws_whenActiveMissing() {
        EIId id = new EIId(UUID.randomUUID(), UUID.randomUUID());

        var ex = assertThrows(ValidationException.class, () ->
                EIEntity.builder()
                        .id(id)
                        .build()
        );
        assertEquals("educationinstructor.active.required", ex.getMessage());
    }
}
