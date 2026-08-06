package local.sop.datawarehouse.workhour.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

class WorkHourIdTest {

    @Test
    void shouldCreate_whenValueIsValid() {
        UUID uuid = UUID.randomUUID();

        WorkHourId id = new WorkHourId(uuid);

        assertNotNull(id);
        assertEquals(uuid, id.value());
    }

    @Test
    void shouldCreate_whenUsingFactoryMethod() {
        UUID uuid = UUID.randomUUID();

        WorkHourId id = WorkHourId.of(uuid);

        assertNotNull(id);
        assertEquals(uuid, id.value());
    }

    @Test
    void shouldCreate_whenUsingNewId() {
        WorkHourId id = WorkHourId.newId();

        assertNotNull(id);
        assertNotNull(id.value());
    }

    @Test
    void shouldThrow_whenValueIsNull() {
        ValidationException ex = assertThrows(ValidationException.class, () -> new WorkHourId(null));

        assertEquals("workhour.id.required", ex.getMessage());
    }
}