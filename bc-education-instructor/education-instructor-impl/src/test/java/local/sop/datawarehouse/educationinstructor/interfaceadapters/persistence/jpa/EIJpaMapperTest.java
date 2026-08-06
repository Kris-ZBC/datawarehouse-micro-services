package local.sop.datawarehouse.educationinstructor.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.datawarehouse.educationinstructor.domain.model.EducationInstructor;
import local.sop.datawarehouse.educationinstructor.domain.model.valueobjects.CreatedAtTimestamp;

class EIJpaMapperTest {

    @Test
    void toEntity_and_toDomain_preserveImportantFields() {
        EIJpaMapper mapper = new EIJpaMapper();

        UUID educationRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        EducationInstructor domain = new EducationInstructor.Builder()
                .id(new CompositeKey(educationRef, instructorRef))
                .active(true)
                .createdAt(new CreatedAtTimestamp(createdAt))
                .build();

        // Domain → Entity
        EIEntity entity = mapper.toEntity(domain);

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
        EIJpaMapper mapper = new EIJpaMapper();

        UUID educationRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();

        EIEntity entity = EIEntity.builder()
                .id(new EIId(educationRef, instructorRef))
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
        EIJpaMapper mapper = new EIJpaMapper();

        UUID educationRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();

        EducationInstructor domain = new EducationInstructor.Builder()
                .id(new CompositeKey(educationRef, instructorRef))
                .active(false)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
                .build();

        EIEntity entity = EIEntity.builder()
                .id(new EIId(educationRef, instructorRef))
                .active(true)
                .build();

        mapper.updateIntoEntity(domain, entity);

        assertFalse(entity.isActive());
    }
}
