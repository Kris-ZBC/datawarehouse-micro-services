package local.sop.sopinfo.instructor.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import local.sop.sopinfo.instructor.domain.model.Instructor;
import local.sop.sopinfo.instructor.domain.model.valueobjects.InstructorId;
import local.sop.sopinfo.instructor.domain.model.valueobjects.PersonRef;

class InstructorRepositoryAdapterTest {

    private final InstructorSpringDataRepository springDataRepository =
            Mockito.mock(InstructorSpringDataRepository.class);

    private final InstructorRepositoryAdapter adapter =
            new InstructorRepositoryAdapter(springDataRepository);

    @Test
    void save_should_map_and_return_domain_object() {
        UUID id = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        Instructor instructor = Instructor.builder()
                .id(InstructorId.of(id))
                .personRef(PersonRef.of(personRef))
                .build();

        InstructorEntity savedEntity = new InstructorEntity();
        savedEntity.setId(id);
        savedEntity.setPersonRef(personRef);

        when(springDataRepository.save(any(InstructorEntity.class)))
                .thenReturn(savedEntity);

        Instructor result = adapter.save(instructor);

        assertEquals(id, result.getId().value());
        assertEquals(personRef, result.getPersonRef().value());
    }

    @Test
    void findAll_should_return_mapped_domain_objects() {
        UUID id = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        InstructorEntity entity = new InstructorEntity();
        entity.setId(id);
        entity.setPersonRef(personRef);

        when(springDataRepository.findAll()).thenReturn(List.of(entity));

        List<Instructor> result = adapter.findAll();

        assertEquals(1, result.size());
        assertEquals(id, result.get(0).getId().value());
        assertEquals(personRef, result.get(0).getPersonRef().value());
    }

    @Test
    void findById_should_return_domain_object_when_found() {
        UUID id = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        InstructorEntity entity = new InstructorEntity();
        entity.setId(id);
        entity.setPersonRef(personRef);

        when(springDataRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<Instructor> result = adapter.findById(InstructorId.of(id));

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId().value());
        assertEquals(personRef, result.get().getPersonRef().value());
    }

    @Test
    void findById_should_return_empty_when_not_found() {
        UUID id = UUID.randomUUID();

        when(springDataRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Instructor> result = adapter.findById(InstructorId.of(id));

        assertFalse(result.isPresent());
    }
}