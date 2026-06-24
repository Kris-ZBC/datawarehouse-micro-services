package local.sop.sopinfo.auditlog.domain.model.valueobjects;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OriginSystemTest {

    // --------------------
    // Constructor tests
    // --------------------

    @Test
    void constructor_shouldThrow_whenNull() {
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            new OriginSystem(null);
        });
        assertEquals("log.origin.system.required", ex.getMessage());
    }

    @Test
    void constructor_shouldThrow_whenBlank() {
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            new OriginSystem("   ");
        });
        assertEquals("log.origin.system.required", ex.getMessage());
    }

    @Test
    void constructor_shouldKeepValue_whenValid() {
        OriginSystem originSystem = new OriginSystem("SystemA");
        assertEquals("SystemA", originSystem.value());
    }

    // --------------------
    // Factory method tests
    // --------------------

    @Test
    void newOriginSystem_shouldThrow_whenNull() {
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            OriginSystem.newOriginSystem(null);
        });
        assertEquals("log.origin.system.required", ex.getMessage());
    }

    @Test
    void newOriginSystem_shouldThrow_whenBlank() {
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            OriginSystem.newOriginSystem("   ");
        });
        assertEquals("log.origin.system.required", ex.getMessage());
    }

    @Test
    void newOriginSystem_shouldCreate_whenValid() {
        OriginSystem originSystem = OriginSystem.newOriginSystem("ValidSystem");
        assertEquals("ValidSystem", originSystem.value());
    }

    // --------------------
    // toString test
    // --------------------

    @Test
    void toString_shouldReturnValue() {
        OriginSystem originSystem = new OriginSystem("SystemName");
        assertEquals("SystemName", originSystem.toString());
    }
}