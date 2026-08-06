package local.sop.datawarehouse.educationline.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Constructor;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class EducationLineEntityTest {

    @Test
    void constructor_and_getters_setters_work() {
        UUID id = UUID.randomUUID();
        UUID educationRef = UUID.randomUUID();

        EducationLineEntity entity = new EducationLineEntity(id, "Math", 1, 2, 3, educationRef, true);
        assertEquals(id, entity.getId());
        assertEquals("Math", entity.getName());
        assertEquals(1, entity.getDurationYears());
        assertEquals(2, entity.getDurationMonths());
        assertEquals(3, entity.getDurationDays());
        assertEquals(educationRef, entity.getEducationRef());
        assertEquals(true, entity.isActive());

        UUID newId = UUID.randomUUID();
        entity.setId(newId);
        entity.setName("Physics");
        entity.setDurationYears(2);
        entity.setDurationMonths(4);
        entity.setDurationDays(5);
        entity.setEducationRef(UUID.randomUUID());
        entity.setActive(false);

        assertEquals(newId, entity.getId());
        assertEquals("Physics", entity.getName());
        assertEquals(2, entity.getDurationYears());
        assertEquals(4, entity.getDurationMonths());
        assertEquals(5, entity.getDurationDays());
        assertEquals(false, entity.isActive());
    }

    @Test
    void default_constructor_exists_for_jpa() throws Exception {
        Constructor<EducationLineEntity> ctor = EducationLineEntity.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        EducationLineEntity entity = ctor.newInstance();
        assertNotNull(entity);
    }
}
