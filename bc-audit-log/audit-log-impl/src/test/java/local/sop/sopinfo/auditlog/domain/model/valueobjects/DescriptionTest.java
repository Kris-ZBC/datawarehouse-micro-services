package local.sop.sopinfo.auditlog.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;


class DescriptionTest {

    @Test
    void constructor_shouldKeepValue_whenNonBlank() {
        Description description = new Description("Some description");

        assertEquals("Some description", description.value());
    }

    @Test
    void constructor_shouldNormalizeBlankToNull() {
        Description description = new Description("   ");

        assertNull(description.value());
    }

    @Test
    void constructor_shouldAllowNull() {
        Description description = new Description(null);

        assertNull(description.value());
    }

    @Test
    void toString_shouldReturnValue_whenNotNull() {
        Description description = new Description("Hello");

        assertEquals("Hello", description.toString());
    }

    @Test
    void toString_shouldReturnEmptyString_whenNull() {
        Description description = new Description(null);

        assertEquals("", description.toString());
    }

    @Test
    void newDescription_shouldThrow_whenNull() {
        assertThrows(ValidationException.class, () -> {
            Description.newDescription(null);
        });
    }

    @Test
    void newDescription_shouldThrow_whenBlank() {
        assertThrows(ValidationException.class, () -> {
            Description.newDescription("   ");
        });
    }
}