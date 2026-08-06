package local.sop.datawarehouse.instructor.interfaceadapters.persistence.jpa;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

import local.sop.datawarehouse.instructor.domain.model.valueobjects.PersonRef;

class InstructorEntityTest {

    @Test
    void constructor_and_getters_setters_should_work() {
        InstructorEntity entity = new InstructorEntity();

        UUID id = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        entity.setId(id);
        entity.setPersonRef(personRef);

        assertEquals(id, entity.getId());
        assertEquals(personRef, entity.getPersonRef());
        assertNull(entity.getVersion());
    }

    @Test
    void protected_constructor_should_be_invokable() {
        InstructorEntity entity = new InstructorEntity();
        assertNotNull(entity);
    }

    @Test
    void build_should_generate_id_when_id_is_not_provided() {
        UUID personRef = UUID.randomUUID();

        InstructorEntity entity = new InstructorEntity.Builder()
                .personRef(PersonRef.of(personRef))
                .Build();

        assertNotNull(entity.getId());
        assertEquals(personRef, entity.getPersonRef());
    }
}