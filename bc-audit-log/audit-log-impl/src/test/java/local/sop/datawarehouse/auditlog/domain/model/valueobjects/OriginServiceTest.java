package local.sop.datawarehouse.auditlog.domain.model.valueobjects;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OriginServiceTest {

    // --------------------
    // Constructor tests
    // --------------------

    @Test
    void constructor_shouldThrow_whenNull() {
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            new OriginService(null);
        });
        assertEquals("log.origin.service.required", ex.getMessage());
    }

    @Test
    void constructor_shouldThrow_whenBlank() {
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            new OriginService("   ");
        });
        assertEquals("log.origin.service.required", ex.getMessage());
    }

    @Test
    void constructor_shouldKeepValue_whenValid() {
        OriginService service = new OriginService("MyService");
        assertEquals("MyService", service.value());
    }

    // --------------------
    // Factory method tests
    // --------------------

    @Test
    void newOriginService_shouldThrow_whenNull() {
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            OriginService.newOriginService(null);
        });
        assertEquals("log.origin.service.required", ex.getMessage());
    }

    @Test
    void newOriginService_shouldThrow_whenBlank() {
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            OriginService.newOriginService("   ");
        });
        assertEquals("log.origin.service.required", ex.getMessage());
    }

    @Test
    void newOriginService_shouldCreate_whenValid() {
        OriginService service = OriginService.newOriginService("ValidService");
        assertEquals("ValidService", service.value());
    }

    // --------------------
    // toString test
    // --------------------

    @Test
    void toString_shouldReturnValue() {
        OriginService service = new OriginService("ServiceName");
        assertEquals("ServiceName", service.toString());
    }
}