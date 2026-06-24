package local.sop.sopinfo.auditlog.domain.model.valueobjects;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OriginComponentTest {

    // --------------------
    // Constructor tests
    // --------------------

    @Test
    void constructor_shouldThrow_whenNull() {
        ValidationException ex = assertThrows(ValidationException.class, () -> new OriginComponent(null));
        assertEquals("log.origin.component.required", ex.getMessage());
    }

    @Test
    void constructor_shouldThrow_whenBlank() {
        ValidationException ex = assertThrows(ValidationException.class, () -> new OriginComponent("   "));
        assertEquals("log.origin.component.required", ex.getMessage());
    }

    @Test
    void constructor_shouldKeepValue_whenValid() {
        OriginComponent component = new OriginComponent("ComponentA");
        assertEquals("ComponentA", component.value());
    }

    // --------------------
    // Factory method tests
    // --------------------

    @Test
    void newOriginComponent_shouldThrow_whenNull() {
        ValidationException ex = assertThrows(ValidationException.class, () -> OriginComponent.newOriginComponent(null));
        assertEquals("log.origin.component.required", ex.getMessage());
    }

    @Test
    void newOriginComponent_shouldThrow_whenBlank() {
        ValidationException ex = assertThrows(ValidationException.class, () -> OriginComponent.newOriginComponent("   "));
        assertEquals("log.origin.component.required", ex.getMessage());
    }

    @Test
    void newOriginComponent_shouldCreate_whenValid() {
        OriginComponent component = OriginComponent.newOriginComponent("ValidComponent");
        assertEquals("ValidComponent", component.value());
    }

    // --------------------
    // toString test
    // --------------------

    @Test
    void toString_shouldReturnValue() {
        OriginComponent component = new OriginComponent("MyComponent");
        assertEquals("MyComponent", component.toString());
    }
}