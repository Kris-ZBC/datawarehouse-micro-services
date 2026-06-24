package local.sop.sopinfo.educationinstructor.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.educationinstructor.domain.model.EducationInstructor;
import local.sop.sopinfo.educationinstructor.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;

class EducationInstructorDomainJpaMapperTest {

    @Test
    void toEntity_and_toDomain_preserveImportantFields() {
        EducationInstructorJpaMapper mapper = new EducationInstructorJpaMapper();

        UUID educationRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        EducationInstructor domain = new EducationInstructor.Builder()
                .id(new CompositeKey(educationRef, instructorRef))
                .active(true)
                .createdAt(new CreatedAtTimestamp(createdAt))
                .build();

        // Domain → Entity
        EducationInstructorEntity entity = mapper.toEntity(domain);

        // Entity → Domain
        EducationInstructor mappedBack = mapper.toDomain(entity);

        assertEquals(educationRef, mappedBack.getId().key1());
        assertEquals(instructorRef, mappedBack.getId().key2());
        assertTrue(mappedBack.isActive());

        // createdAt should be preserved and not null
        assertNotNull(mappedBack.getCreatedAt());
        assertEquals(entity.getCreatedAt(), mappedBack.getCreatedAt().value());
    }

    @Test
    void toDomain_mapsEntityCorrectly() {
        EducationInstructorJpaMapper mapper = new EducationInstructorJpaMapper();

        UUID educationRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();

        EducationInstructorEntity entity = EducationInstructorEntity.builder()
                .id(new EducationInstructorId(educationRef, instructorRef))
                .active(true)
                .build();

        EducationInstructor domain = mapper.toDomain(entity);

        assertEquals(educationRef, domain.getId().key1());
        assertEquals(instructorRef, domain.getId().key2());
        assertTrue(domain.isActive());
        assertNotNull(domain.getCreatedAt());
    }

    @Test
    void updateIntoEntity_updatesActiveField() {
        EducationInstructorJpaMapper mapper = new EducationInstructorJpaMapper();

        UUID educationRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();

        EducationInstructor domain = new EducationInstructor.Builder()
                .id(new CompositeKey(educationRef, instructorRef))
                .active(false)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
                .build();

        EducationInstructorEntity entity = EducationInstructorEntity.builder()
                .id(new EducationInstructorId(educationRef, instructorRef))
                .active(true)
                .build();

        mapper.updateIntoEntity(domain, entity);

        assertFalse(entity.isActive());
    }
}
