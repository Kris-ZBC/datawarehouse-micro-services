package local.sop.sopinfo.instructor.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

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
}