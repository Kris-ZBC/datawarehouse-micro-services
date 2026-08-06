package local.sop.datawarehouse.anonymize.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public class AnonymizeEntityTest {

     @Test
    void create_WhenIdIsNull_ShouldThrowValidationException() {
        // When / Then
        assertThrows(
                ValidationException.class,
                () -> AnonymizeEntity.create(
                        null,
                        UUID.randomUUID()
                )
        );
    }

     @Test
    void create_WhenPersonRefIsNull_ShouldThrowValidationException() {
        // When / Then
        assertThrows(
                ValidationException.class,
                () -> AnonymizeEntity.create(
                        UUID.randomUUID(),
                        null
                )
        );
    }
    
}
