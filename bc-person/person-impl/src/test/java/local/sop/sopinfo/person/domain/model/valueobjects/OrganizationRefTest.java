package local.sop.sopinfo.person.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

class OrganizationRefTest {

    @Test
    void shouldCreateOrganizationRefWhenValueIsValid() {
        UUID uuid = UUID.randomUUID();

        OrganizationRef organizationRef = new OrganizationRef(uuid);

        assertEquals(uuid, organizationRef.value());
    }

    @Test
    void shouldCreateOrganizationRefFromStringWhenValueIsValid() {
        UUID uuid = UUID.randomUUID();

        OrganizationRef organizationRef = OrganizationRef.fromString(uuid.toString());

        assertEquals(uuid, organizationRef.value());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsNull() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> new OrganizationRef(null)
        );

        assertEquals("person.organizationref.blank", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsBlank() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> OrganizationRef.fromString("   ")
        );

        assertEquals("person.organizationref.blank", exception.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenValueIsInvalid() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> OrganizationRef.fromString("not-a-uuid")
        );

        assertEquals("person.organizationref.invalid", exception.getMessage());
    }
}