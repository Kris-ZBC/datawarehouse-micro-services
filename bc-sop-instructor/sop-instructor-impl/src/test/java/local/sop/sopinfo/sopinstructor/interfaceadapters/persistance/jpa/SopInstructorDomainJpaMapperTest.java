package local.sop.sopinfo.sopinstructor.interfaceadapters.persistance.jpa;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.sopinstructor.domain.model.SopInstructor;
import local.sop.sopinfo.sopinstructor.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.sopinfo.sopinstructor.interfaceadapters.persistence.jpa.SopInstructorDomainJpaMapper;
import local.sop.sopinfo.sopinstructor.interfaceadapters.persistence.jpa.SopInstructorEntity;
import local.sop.sopinfo.sopinstructor.interfaceadapters.persistence.jpa.SopInstructorId;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;

class SopInstructorDomainJpaMapperTest {

    @Test
    void toEntity_and_toDomain_preserveImportantFields() {
        SopInstructorDomainJpaMapper mapper = new SopInstructorDomainJpaMapper();

        UUID sopRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        SopInstructor domain = new SopInstructor.Builder()
                .id(new CompositeKey(sopRef, instructorRef))
                .active(true)
                .createdAt(new CreatedAtTimestamp(createdAt))
                .build();

        // Domain → Entity
        SopInstructorEntity entity = mapper.toEntity(domain);

        // Entity → Domain
        SopInstructor mappedBack = mapper.toDomain(entity);

        assertEquals(sopRef, mappedBack.getId().key1());
        assertEquals(instructorRef, mappedBack.getId().key2());
        assertTrue(mappedBack.isActive());

        // createdAt should be preserved and not null
        assertNotNull(mappedBack.getCreatedAt());
        assertEquals(entity.getCreatedAt(), mappedBack.getCreatedAt().value());
    }

    @Test
    void toDomain_mapsEntityCorrectly() {
        SopInstructorDomainJpaMapper mapper = new SopInstructorDomainJpaMapper();

        UUID sopRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();

        SopInstructorEntity entity = SopInstructorEntity.builder()
                .id(new SopInstructorId(sopRef, instructorRef))
                .active(true)
                .build();

        SopInstructor domain = mapper.toDomain(entity);

        assertEquals(sopRef, domain.getId().key1());
        assertEquals(instructorRef, domain.getId().key2());
        assertTrue(domain.isActive());
        assertNotNull(domain.getCreatedAt());
    }

    @Test
    void updateIntoEntity_updatesActiveField() {
        SopInstructorDomainJpaMapper mapper = new SopInstructorDomainJpaMapper();

        UUID sopRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();

        SopInstructor domain = new SopInstructor.Builder()
                .id(new CompositeKey(sopRef, instructorRef))
                .active(false)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
                .build();

        SopInstructorEntity entity = SopInstructorEntity.builder()
                .id(new SopInstructorId(sopRef, instructorRef))
                .active(true)
                .build();

        mapper.updateIntoEntity(domain, entity);

        assertFalse(entity.isActive());
    }
}
