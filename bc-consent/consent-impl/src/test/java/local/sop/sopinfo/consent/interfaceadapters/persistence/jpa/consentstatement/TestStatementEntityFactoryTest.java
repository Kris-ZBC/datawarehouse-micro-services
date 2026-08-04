package local.sop.sopinfo.consent.interfaceadapters.persistence.jpa.consentstatement;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import local.sop.sopinfo.consent.domain.model.valueobject.ConsentStatementRef;
import local.sop.sopinfo.consent.interfaceadapters.persistence.jpa.consentstatement.factory.TestStatementEntityFactory;

public class TestStatementEntityFactoryTest {
    private final TestStatementEntityFactory factory = new TestStatementEntityFactory();

    @Test
    @DisplayName("Should create ConsentStatementEntity when statementRef is provided.")
    void shouldCreateStatementEntity(){
        // Given
        UUID uuid = UUID.randomUUID();
        ConsentStatementRef statementRef = ConsentStatementRef.of(uuid);

        // When
        ConsentStatementEntity result = factory.createStatementEntity(statementRef);
        
        // Then
        assertNotNull(result);
        assertEquals(uuid, result.getId());
        assertEquals("test", result.getStatementText());
        assertTrue(result.isActive());
    }

    @Test
    @DisplayName("Should return null when statementRef is null.")
    void shouldReturnNullForNullStatementRef(){
        // When
        ConsentStatementEntity result = factory.createStatementEntity(null);

        // Then
        assertNull(result);
    }
}
 