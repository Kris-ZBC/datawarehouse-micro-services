package local.sop.sopinfo.instructor.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.instructor.domain.model.Instructor;
import local.sop.sopinfo.instructor.domain.model.valueobjects.InstructorId;
import local.sop.sopinfo.instructor.domain.model.valueobjects.PersonRef;

class InstructorJpaMapperTest {

    @Test
    void toEntity_and_toDomain_roundTrip_shouldPreserveValues() {
        UUID id = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        Instructor domain = Instructor.builder()
                .id(InstructorId.of(id))
                .personRef(PersonRef.of(personRef))
                .build();

        InstructorEntity entity = InstructorJpaMapper.toEntity(domain);
        Instructor mappedBack = InstructorJpaMapper.toDomain(entity);

        assertEquals(id, mappedBack.getId().value());
        assertEquals(personRef, mappedBack.getPersonRef().value());
    }

    @Test
    void toEntity_shouldMapDomainToEntity() {
        UUID id = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        Instructor domain = Instructor.builder()
                .id(InstructorId.of(id))
                .personRef(PersonRef.of(personRef))
                .build();

        InstructorEntity entity = InstructorJpaMapper.toEntity(domain);

        assertEquals(id, entity.getId());
        assertEquals(personRef, entity.getPersonRef());
    }

    @Test
    void toDomain_shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        InstructorEntity entity = new InstructorEntity();
        entity.setId(id);
        entity.setPersonRef(personRef);

        Instructor domain = InstructorJpaMapper.toDomain(entity);

        assertEquals(id, domain.getId().value());
        assertEquals(personRef, domain.getPersonRef().value());
    }

    @Test
    void privateConstructor_shouldBeInvokable_forCoverage() throws Exception {
        var ctor = InstructorJpaMapper.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object instance = ctor.newInstance();

        assertNotNull(instance);
    }
}