package local.sop.sopinfo.consent.interfaceadapters.persistence.jpa.consentstatement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import local.sop.sopinfo.consent.domain.model.valueobject.ConsentStatementRef;

import local.sop.sopinfo.consent.interfaceadapters.persistence.jpa.consentstatement.factory.ProdStatementEntityFactory;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

@ActiveProfiles({"prod", "h2"})
@TestPropertySource(properties = {
    "security.enabled=false"
})
@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ProdStatementEntityFactoryTest {
        @Mock
    private ConsentStatementSpringDataRepository repository;

    @InjectMocks
    private ProdStatementEntityFactory factory;

    private static final UUID STATEMENT_ID = UUID.fromString("111e4567-e89b-12d3-a456-426614174111");

    @Test
    @DisplayName("Should return null when statementRef is null")
    void createStatementEntity_WhenRefIsNull_ShouldReturnNull() {
        // When
        ConsentStatementEntity result = factory.createStatementEntity(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should return full entity from repository when ref is valid")
    void createStatementEntity_WhenRefIsValid_ShouldReturnEntityFromRepository() {
        // Given
        ConsentStatementRef ref = new ConsentStatementRef(STATEMENT_ID);
        ConsentStatementEntity expectedEntity = ConsentStatementEntity.builder()
                .id(STATEMENT_ID)
                .statementText("Marketing consent statement")
                .active(true)
                .build();

        when(repository.findById(STATEMENT_ID)).thenReturn(Optional.of(expectedEntity));

        // When
        ConsentStatementEntity result = factory.createStatementEntity(ref);

        // Then
        assertNotNull(result);
        assertEquals(STATEMENT_ID, result.getId());
        assertEquals("Marketing consent statement", result.getStatementText());
        assertTrue(result.isActive());
        verify(repository).findById(STATEMENT_ID);
    }

    @Test
    @DisplayName("Should throw ValidationException when statement not found in repository")
    void createStatementEntity_WhenNotFound_ShouldThrowValidationException() {
        // Given
        ConsentStatementRef ref = new ConsentStatementRef(STATEMENT_ID);

        when(repository.findById(STATEMENT_ID)).thenReturn(Optional.empty());

        // When & Then
        ValidationException ex = assertThrows(ValidationException.class,
                () -> factory.createStatementEntity(ref));

        assertEquals("consentstatement.notfound", ex.getMessage());
        verify(repository).findById(STATEMENT_ID);
    }

    @Test
    @DisplayName("Should return entity with statementText populated — not just the id stub")
    void createStatementEntity_ShouldNotReturnStubWithNullStatementText() {
        // Given — this is the exact failure mode of the old implementation
        ConsentStatementRef ref = new ConsentStatementRef(STATEMENT_ID);
        ConsentStatementEntity fullEntity = ConsentStatementEntity.builder()
                .id(STATEMENT_ID)
                .statementText("Some real statement text")
                .active(true)
                .build();

        when(repository.findById(STATEMENT_ID)).thenReturn(Optional.of(fullEntity));

        // When
        ConsentStatementEntity result = factory.createStatementEntity(ref);

        // Then — statementText must never be null in prod
        assertNotNull(result.getStatementText(),
                "statementText must not be null — ProdStatementEntityFactory must fetch from DB, not build a stub");
    }
}
