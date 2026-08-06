package local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.enums.ConsentType;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.datawarehouse.consent.domain.model.Consent;
import local.sop.datawarehouse.consent.domain.model.ConsentStatement;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentId;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentStatementRef;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentStatementValue;
import local.sop.datawarehouse.consent.domain.model.valueobject.PersonRef;
import local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement.ConsentStatementEntity;
import local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement.ConsentStatementRepositoryJpaAdapter;
import local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement.ConsentStatementSpringDataRepository;

@SpringBootTest
@ActiveProfiles({"test", "h2"})
@TestPropertySource(properties = {
    "security.enabled=false"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ConsentStatementRepositoryJpaAdapterTest {

    @Autowired
    private ConsentStatementRepositoryJpaAdapter repositoryAdapter;

    @Autowired
    private ConsentStatementSpringDataRepository springDataRepository;

    private static final UUID STATEMENT_ID_1 = UUID.fromString("111e4567-e89b-12d3-a456-426614174111");
    private static final UUID STATEMENT_ID_2 = UUID.fromString("222e4567-e89b-12d3-a456-426614174222");

    @BeforeEach
    void setUp() {
        // Clean database before each test
        springDataRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Should save consent statement with consents")
    @Transactional 
    void shouldSaveConsentStatementWithConsents() {
        // Given
        Consent consent1 = Consent.builder()
                .id(ConsentId.of(UUID.randomUUID()))
                .personRef(PersonRef.of(UUID.randomUUID()))
                .consentStatementRef(ConsentStatementRef.of(STATEMENT_ID_1))
                .status(ConsentStatus.ACTIVE)
                .purpose(ConsentPurpose.MARKETING)
                .type(ConsentType.OPTIONAL)
                .build();

        Consent consent2 = Consent.builder()
                .id(ConsentId.of(UUID.randomUUID()))
                .personRef(PersonRef.of(UUID.randomUUID()))
                .consentStatementRef(ConsentStatementRef.of(STATEMENT_ID_1))
                .status(ConsentStatus.WITHDRAWN)
                .purpose(ConsentPurpose.MARKETING)
                .type(ConsentType.OPTIONAL)
                .build();

        ConsentStatement statement = ConsentStatement.builder()
                .id(ConsentStatementRef.of(STATEMENT_ID_1))
                .statementText(new ConsentStatementValue("Marketing consent"))
                .active(true)
                .consents(Set.of(consent1, consent2))
                .build();

        // When
        ConsentStatement saved = repositoryAdapter.save(statement);

        // Then
        assertNotNull(saved);
        assertEquals(STATEMENT_ID_1, saved.getId().value());
        assertEquals("Marketing consent", saved.getStatementText());
        assertTrue(saved.isActive());
        assertEquals(2, saved.getConsents().size());
        
        // Verify persistence
        List<ConsentStatementEntity> entities = springDataRepository.findAll();
        assertEquals(1, entities.size());
        
        ConsentStatementEntity entity = entities.get(0);
        assertEquals(STATEMENT_ID_1, entity.getId());
        assertEquals("Marketing consent", entity.getStatementText());
        assertEquals(2, entity.getConsents().size());
    }

    @Test
    @Order(2)
    @DisplayName("Should find all consent statements")
    @Transactional
    void shouldFindAllConsentStatements() {
        // Given - create test data
        ConsentStatement statement1 = ConsentStatement.builder()
                .id(ConsentStatementRef.of(STATEMENT_ID_1))
                .statementText(new ConsentStatementValue("Marketing consent"))
                .active(true)
                .build();

        ConsentStatement statement2 = ConsentStatement.builder()
                .id(ConsentStatementRef.of(STATEMENT_ID_2))
                .statementText(new ConsentStatementValue("Analytics consent"))
                .active(false)
                .build();

        repositoryAdapter.save(statement1);
        repositoryAdapter.save(statement2);

        // When
        List<ConsentStatement> result = repositoryAdapter.findAll();

        // Then
        assertEquals(2, result.size());
        
        Map<UUID, ConsentStatement> resultById = result.stream()
                .collect(Collectors.toMap(s -> s.getId().value(), s -> s));
        
        assertTrue(resultById.containsKey(STATEMENT_ID_1));
        assertTrue(resultById.containsKey(STATEMENT_ID_2));
        
        assertEquals("Marketing consent", resultById.get(STATEMENT_ID_1).getStatementText());
        assertTrue(resultById.get(STATEMENT_ID_1).isActive());
        
        assertEquals("Analytics consent", resultById.get(STATEMENT_ID_2).getStatementText());
        assertFalse(resultById.get(STATEMENT_ID_2).isActive());
    }

    @Test
    @Order(3)
    @DisplayName("Should find consent statement by id")
    @Transactional
    void shouldFindConsentStatementById() {
        // Given
        ConsentStatement statement = ConsentStatement.builder()
                .id(ConsentStatementRef.of(STATEMENT_ID_1))
                .statementText(new ConsentStatementValue("Test statement"))
                .active(true)
                .build();

        ConsentStatement saved = repositoryAdapter.save(statement);

        // When
        Optional<ConsentStatement> result = repositoryAdapter.findById(ConsentStatementRef.of(STATEMENT_ID_1));

        // Then
        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.get().getId());
        assertEquals("Test statement", result.get().getStatementText());
    }

    @Test
    @DisplayName("Should return empty when not found")
    void shouldReturnEmptyWhenNotFound() {
        // When
        Optional<ConsentStatement> result = repositoryAdapter.findById(ConsentStatementRef.of(UUID.randomUUID()));

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @Order(4)
    @DisplayName("Should update statement text and active status")
    @Transactional
    void shouldUpdateStatementTextAndActiveStatus() {
        // Given
        ConsentStatement statement = ConsentStatement.builder()
                .id(ConsentStatementRef.of(STATEMENT_ID_1))
                .statementText(new ConsentStatementValue("Original text"))
                .active(true)
                .build();

        ConsentStatement saved = repositoryAdapter.save(statement);

        // When
        ConsentStatement updated = repositoryAdapter.updateStatement(
                saved.getId(), 
                false, 
                "Updated text"
        );

        // Then
        assertNotNull(updated);
        assertEquals(STATEMENT_ID_1, updated.getId().value());
        assertEquals("Updated text", updated.getStatementText());
        assertFalse(updated.isActive());

        // Verify in database
        Optional<ConsentStatementEntity> dbEntity = springDataRepository.findById(STATEMENT_ID_1);
        assertTrue(dbEntity.isPresent());
        assertEquals("Updated text", dbEntity.get().getStatementText());
        assertFalse(dbEntity.get().isActive());
    }

    @Test
    @Order(5)
    @DisplayName("Should update only active status")
    @Transactional
    void shouldUpdateOnlyActiveStatus() {
        // Given
        ConsentStatement statement = ConsentStatement.builder()
                .id(ConsentStatementRef.of(STATEMENT_ID_1))
                .statementText(new ConsentStatementValue("Test text"))
                .active(true)
                .build();

        ConsentStatement saved = repositoryAdapter.save(statement);

        // When
        ConsentStatement updated = repositoryAdapter.updateActiveStatus(saved.getId(), false);

        // Then
        assertNotNull(updated);
        assertEquals("Test text", updated.getStatementText()); // Uændret
        assertFalse(updated.isActive()); // Ændret
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent statement")
    @Transactional
    void shouldThrowExceptionWhenUpdatingNonExistentStatement() {
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            repositoryAdapter.updateStatement(ConsentStatementRef.of(UUID.randomUUID()), true, "New text");
        });
        assertEquals("consentstatement.notfound", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when updating active status for non-existent statement")
    @Transactional
    void shouldThrowExceptionWhenUpdatingActiveStatusForNonExistentStatement() {
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            repositoryAdapter.updateActiveStatus(ConsentStatementRef.of(UUID.randomUUID()), false);
        });
        assertEquals("consentstatement.notfound", exception.getMessage());
    }

    @Test
    @Order(6)
    @DisplayName("Should handle complex scenario with multiple statements and consents")
    @Transactional
    void shouldHandleComplexScenario() {
        // Given
        ConsentStatement marketingStatement = ConsentStatement.builder()
                .id(ConsentStatementRef.of(STATEMENT_ID_1))
                .statementText(new ConsentStatementValue("Marketing consent"))
                .active(true)
                .build();

        ConsentStatement analyticsStatement = ConsentStatement.builder()
                .id(ConsentStatementRef.of(STATEMENT_ID_2))
                .statementText(new ConsentStatementValue("Analytics consent"))
                .active(true)
                .build();

        // Save statements
        repositoryAdapter.save(marketingStatement);
        repositoryAdapter.save(analyticsStatement);

        // When - update marketing statement, deactivate analytics
        repositoryAdapter.updateStatement(ConsentStatementRef.of(STATEMENT_ID_1), false, "Updated marketing consent");
        repositoryAdapter.updateActiveStatus(ConsentStatementRef.of(STATEMENT_ID_2), false);

        // Then
        List<ConsentStatement> allStatements = repositoryAdapter.findAll();
        assertEquals(2, allStatements.size());

        ConsentStatement updatedMarketing = allStatements.stream()
                .filter(s -> s.getId().value().equals(STATEMENT_ID_1))
                .findFirst()
                .orElseThrow();

        ConsentStatement updatedAnalytics = allStatements.stream()
                .filter(s -> s.getId().value().equals(STATEMENT_ID_2))
                .findFirst()
                .orElseThrow();

        assertEquals("Updated marketing consent", updatedMarketing.getStatementText());
        assertFalse(updatedMarketing.isActive());

        assertEquals("Analytics consent", updatedAnalytics.getStatementText()); // Uændret
        assertFalse(updatedAnalytics.isActive());
    }

    @Test
    @Transactional
    void compensate_WhenSagaOutcomeIsCompensate_ShouldDeleteAndReturnTrue() {
        // Given
        ConsentStatementEntity entity = ConsentStatementEntity.builder()
            .id(STATEMENT_ID_1)
            .statementText("Test")
            .active(true)
            .build();
        springDataRepository.save(entity);
        ConsentStatementRef domainId = ConsentStatementRef.of(STATEMENT_ID_1);

        // When
        Boolean result = repositoryAdapter.compensate(domainId, SagaOutcome.COMPENSATE);

        // Then
        assertTrue(result);
        assertTrue(springDataRepository.findById(STATEMENT_ID_1).isEmpty());
    }

    @Test
    @Transactional
    void compensate_WhenSagaOutcomeIsNotCompensate_ShouldThrowConflictException() {
        // Given
        ConsentStatementEntity entity = ConsentStatementEntity.builder()
            .id(STATEMENT_ID_1)
            .statementText("Test")
            .active(true)
            .build();
        springDataRepository.save(entity);
        ConsentStatementRef domainId = ConsentStatementRef.of(STATEMENT_ID_1);

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            repositoryAdapter.compensate(domainId, SagaOutcome.PARTIAL_FAILURE);
        });
        
        assertEquals("compensate.wrong_state", exception.getMessage());
        assertTrue(springDataRepository.findById(STATEMENT_ID_1).isPresent());
    }

    @Test
    @Transactional
    void compensate_WhenEntityDoesNotExist_ShouldReturnFalse() {
        // Given
        ConsentStatementRef domainId = ConsentStatementRef.of(STATEMENT_ID_1);

        // When
        Boolean result = repositoryAdapter.compensate(domainId, SagaOutcome.COMPENSATE);

        // Then
        assertFalse(result);
    }

    @Test
    @Transactional
    void compensate_WhenSagaOutcomeIsNotCompensateAndEntityDoesNotExist_ShouldThrowConflictException() {
        // Given
        ConsentStatementRef domainId = ConsentStatementRef.of(STATEMENT_ID_1);

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            repositoryAdapter.compensate(domainId, SagaOutcome.SUCCEEDED);
        });
        
        assertEquals("compensate.wrong_state", exception.getMessage());
    }
}