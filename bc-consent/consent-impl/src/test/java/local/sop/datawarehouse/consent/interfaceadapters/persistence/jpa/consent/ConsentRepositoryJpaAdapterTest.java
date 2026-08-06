package local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consent;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.enums.ConsentType;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.datawarehouse.consent.domain.model.Consent;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentId;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentStatementRef;
import local.sop.datawarehouse.consent.domain.model.valueobject.PersonRef;
import local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement.ConsentStatementEntity;
import local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement.ConsentStatementSpringDataRepository;
import local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement.factory.ProdStatementEntityFactory;
import local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement.factory.StatementEntityFactory;
import local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement.factory.TestStatementEntityFactory;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.profiles.active=test",
    "security.enabled=false"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import({ProdStatementEntityFactory.class, TestStatementEntityFactory.class})

public class ConsentRepositoryJpaAdapterTest {


    private ConsentRepositoryJpaAdapter adapter;
    private ConsentJpaMapper mapper;

    @Autowired
    private ConsentSpringDataRepository repository;

    @Autowired
    private ConsentStatementSpringDataRepository statementRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired // Brug den rigtige factory, ikke mock
    private StatementEntityFactory statementFactory;

    private ConsentStatementEntity testStatement;
    private PersonRef testPersonRef;
    private ConsentStatementRef testStatementRef;

    @BeforeEach
    void setUp() {
        // Opret en mock factory der returnerer den PERSISTEREDE test statement
        statementFactory = new StatementEntityFactory() {
            @Override
            public ConsentStatementEntity createStatementEntity(ConsentStatementRef statementRef) {
                if (statementRef == null) {
                    return null;
                }
                // Hvis det er vores test statement ref, returnér den persisterede
                if (statementRef.equals(testStatementRef)) {
                    return testStatement; // Denne er allerede persisteret!
                }
                // For andre refs, opret og persister ny entity
                ConsentStatementEntity newStatement = ConsentStatementEntity.builder()
                    .id(statementRef.value())
                    .statementText("test")
                    .active(true)
                    .build();
                entityManager.persistAndFlush(newStatement);
                return newStatement;
            }
        };

        mapper = new ConsentJpaMapper(statementFactory);
        adapter = new ConsentRepositoryJpaAdapter(repository, statementRepository, mapper);
        
        // create test statement FØR factory setup
        testStatement = ConsentStatementEntity.builder()
            .id(UUID.randomUUID())
            .statementText("Test statement")
            .active(true)
            .build();
        entityManager.persistAndFlush(testStatement);

        testPersonRef = new PersonRef(UUID.randomUUID());
        testStatementRef = new ConsentStatementRef(testStatement.getId());
    }

    @Test
    @Transactional
    void testGrantConsent_ShouldUpdateStatementAndSetConsent() {
        // Given
        Consent grantedConsent = Consent.builder()
            .personRef(testPersonRef)
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .purpose(ConsentPurpose.MARKETING)
            .type(ConsentType.OPTIONAL)
            .build();

        // When
        Consent result = adapter.save(grantedConsent);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(ConsentStatus.ACTIVE, result.getStatus());
        assertEquals(testPersonRef, result.getPersonRef());
        assertEquals(testStatementRef, result.getConsentStatementRef());

        // Verify database state
        ConsentEntity savedEntity = entityManager.find(ConsentEntity.class, result.getId().value());
        assertNotNull(savedEntity);
        assertEquals(ConsentStatus.ACTIVE, savedEntity.getStatus());
        assertEquals(testPersonRef.value(), savedEntity.getPersonReference());
        assertEquals(testStatement.getId(), savedEntity.getConsentStatement().getId());

        // Verify statement is still active and has consent
        ConsentStatementEntity updatedStatement = entityManager.find(ConsentStatementEntity.class, testStatement.getId());
        assertTrue(updatedStatement.isActive());
        assertEquals(1, updatedStatement.getConsents().size());
        assertTrue(updatedStatement.getConsents().contains(savedEntity));
    }

    
    @Test
    void testRevokeConsent_ShouldNotRemoveConsentFromStatement() {
        // Given - først opret en granted consent
        Consent grantedConsent = Consent.builder()
            .personRef(testPersonRef)
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .purpose(ConsentPurpose.MARKETING)
            .type(ConsentType.OPTIONAL)
            .build();

        Consent savedConsent = adapter.save(grantedConsent);

        // When - revoke consent
        Consent revokedConsent = savedConsent.withStatus(ConsentStatus.WITHDRAWN);
        Consent result = adapter.update(revokedConsent);

        // Then
        assertNotNull(result);
        assertEquals(ConsentStatus.WITHDRAWN, result.getStatus());

        // Verify database state
        ConsentEntity savedEntity = entityManager.find(ConsentEntity.class, result.getId().value());
        assertEquals(ConsentStatus.WITHDRAWN, savedEntity.getStatus());

        // Verify consent is still in statement's consent set
        ConsentStatementEntity statement = entityManager.find(ConsentStatementEntity.class, testStatement.getId());
        assertEquals(1, statement.getConsents().size());
        assertTrue(statement.getConsents().contains(savedEntity));
    }
  
    @Test
    void testMultipleRevokedConsents_ShouldDeactivateStatement() {
        // Given - opret flere consents for samme statement
        Consent consent1 = Consent.builder()
            .personRef(new PersonRef(UUID.randomUUID()))
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .purpose(ConsentPurpose.MARKETING)
            .type(ConsentType.REQUIRED)
            .build();

        Consent consent2 = Consent.builder()
            .personRef(new PersonRef(UUID.randomUUID()))
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .purpose(ConsentPurpose.THIRD_PARTY_SHARING)
            .type(ConsentType.REQUIRED)
            .build();

        Consent saved1 = adapter.save(consent1);
        Consent saved2 = adapter.save(consent2);

        // Verify statement has 2 granted consents
        ConsentStatementEntity statement = entityManager.find(ConsentStatementEntity.class, testStatement.getId());
        assertEquals(2, statement.getConsents().size());
        assertTrue(statement.isActive());

        // When - revoke alle consents
        adapter.update(saved1.withStatus(ConsentStatus.WITHDRAWN));
        adapter.update(saved2.withStatus(ConsentStatus.WITHDRAWN));

        // Then - statement skal være deaktiveret
        //entityManager.flush();
        statement = entityManager.find(ConsentStatementEntity.class, testStatement.getId());
        assertFalse(statement.isActive());
        assertEquals(2, statement.getConsents().size()); // consents er stadig i set
    }
     
    @Test
    void testMixedConsentStatuses_ShouldKeepStatementActive() {
        // Given - opret flere consents med forskellig status
        Consent grantedConsent = Consent.builder()
            .personRef(new PersonRef(UUID.randomUUID()))
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .purpose(ConsentPurpose.MARKETING)
            .type(ConsentType.ONE_TIME)
            .build();

        Consent revokedConsent = Consent.builder()
            .personRef(new PersonRef(UUID.randomUUID()))
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.WITHDRAWN)
            .purpose(ConsentPurpose.MARKETING)
            .type(ConsentType.REQUIRED)
            .build();

        adapter.save(grantedConsent);
        adapter.save(revokedConsent);

        // When & Then - statement skal være aktivt fordi der er en granted consent
        ConsentStatementEntity statement = entityManager.find(ConsentStatementEntity.class, testStatement.getId());
        assertTrue(statement.isActive());
        assertEquals(2, statement.getConsents().size());
    }
     

    @Test
    void testFindByIdWhenExists() {
        // Given
        Consent consent = Consent.builder()
            .personRef(testPersonRef)
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .purpose(ConsentPurpose.MARKETING)
            .type(ConsentType.ONE_TIME)
            .build();

        Consent saved = adapter.save(consent);

        // When
        Optional<Consent> result = adapter.findById(saved.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.get().getId());
        assertEquals(ConsentStatus.ACTIVE, result.get().getStatus());
        assertEquals(testPersonRef, result.get().getPersonRef());
        assertEquals(testStatementRef, result.get().getConsentStatementRef());
    }
    
    @Test
    void testFindByIdWhenNotExists() {
        // When
        Optional<Consent> result = adapter.findById(new ConsentId(UUID.randomUUID()));

        // Then
        assertFalse(result.isPresent());
    }

     

    @Test
    void testFindByPersonAndStatementReferenceWhenExists() {
        // Given
        Consent consent = Consent.builder()
            .personRef(testPersonRef)
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .purpose(ConsentPurpose.MARKETING)
            .type(ConsentType.ONE_TIME)
            .build();

        adapter.save(consent);

        // When
        Optional<Consent> result = adapter.findByPersonAndStatementReference(testPersonRef, testStatementRef);

        // Then
        assertTrue(result.isPresent());
        assertEquals(ConsentStatus.ACTIVE, result.get().getStatus());
    }
     
    @Test
    void testFindByPersonAndStatementReferenceWhenNotExists() {
        // When
        Optional<Consent> result = adapter.findByPersonAndStatementReference(
            new PersonRef(UUID.randomUUID()), 
            new ConsentStatementRef(UUID.randomUUID())
        );

        // Then
        assertFalse(result.isPresent());
    }
    
    @Test
    void testFindByPersonReference() {
        // Given
        Consent consent1 = Consent.builder()
            .personRef(testPersonRef)
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .purpose(ConsentPurpose.MARKETING)
            .type(ConsentType.ONE_TIME)
            .build();

        ConsentStatementEntity statement2 = ConsentStatementEntity.builder()
            .id(UUID.randomUUID())
            .statementText("Another statement")
            .active(true)
            .build();

        Consent consent2 = Consent.builder()
            .personRef(testPersonRef)
            .consentStatementRef(new ConsentStatementRef(statement2.getId()))
            .status(ConsentStatus.WITHDRAWN)
            .purpose(ConsentPurpose.ANALYTICS)
            .type(ConsentType.OPTIONAL)
            .build();

        adapter.save(consent1);
        adapter.save(consent2);

        // When
        List<Consent> result = adapter.findByPersonReference(testPersonRef);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getStatus() == ConsentStatus.ACTIVE));
        assertTrue(result.stream().anyMatch(c -> c.getStatus() == ConsentStatus.WITHDRAWN));
    }
    
    @Test
    void testFindByStatusAndPurposeAndType() {
        // Given
        Consent consent1 = Consent.builder()
            .personRef(new PersonRef(UUID.randomUUID()))
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .purpose(ConsentPurpose.MARKETING)
            .type(ConsentType.ONE_TIME)
            .build();
        
        Consent consent2 = Consent.builder()
            .personRef(new PersonRef(UUID.randomUUID()))
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .purpose(ConsentPurpose.MARKETING)
            .type(ConsentType.ONE_TIME)
            .build();

        Consent consent3 = Consent.builder()
            .personRef(new PersonRef(UUID.randomUUID()))
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.WITHDRAWN)
            .purpose(ConsentPurpose.MARKETING)
            .type(ConsentType.OPTIONAL)
            .build();

        adapter.save(consent1);
        adapter.save(consent2);
        adapter.save(consent3);

        // When
        List<Consent> result = adapter.findByStatusAndPurposeAndType(
            ConsentStatus.ACTIVE, 
            ConsentPurpose.MARKETING, 
            ConsentType.ONE_TIME
        );

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(c -> c.getStatus() == ConsentStatus.ACTIVE));
    }

    
    @Test
    void testFindAll() {
        // Given
        Consent consent1 = Consent.builder()
            .personRef(new PersonRef(UUID.randomUUID()))
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .purpose(ConsentPurpose.MARKETING)
            .type(ConsentType.ONE_TIME)
            .build();
        adapter.save(consent1);

        Consent consent2 = Consent.builder()
            .personRef(new PersonRef(UUID.randomUUID()))
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.WITHDRAWN)
            .purpose(ConsentPurpose.ANALYTICS)
            .type(ConsentType.OPTIONAL)
            .build();
        adapter.save(consent2);

        // When
        List<Consent> result = adapter.findAll();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getStatus() == ConsentStatus.ACTIVE));
        assertTrue(result.stream().anyMatch(c -> c.getStatus() == ConsentStatus.WITHDRAWN));
    }
    
    @Test
    void testUpdateConsent() {
        // Given
        Consent originalConsent = Consent.builder()
            .personRef(testPersonRef)
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .purpose(ConsentPurpose.MARKETING)
            .type(ConsentType.ONE_TIME)
            .build();

        Consent savedConsent = adapter.save(originalConsent);

        // When
        Consent updatedConsent = savedConsent
            .withStatus(ConsentStatus.WITHDRAWN)
            .withPurpose(ConsentPurpose.ANALYTICS)
            .withType(ConsentType.OPTIONAL);

        Consent result = adapter.update(updatedConsent);

        // Then
        assertNotNull(result);
        assertEquals(savedConsent.getId(), result.getId());
        assertEquals(ConsentStatus.WITHDRAWN, result.getStatus());
        assertEquals(ConsentPurpose.ANALYTICS, result.getPurpose());
        assertEquals(ConsentType.OPTIONAL, result.getType());

        // Verify database state
        ConsentEntity entity = entityManager.find(ConsentEntity.class, result.getId().value());
        assertEquals(ConsentStatus.WITHDRAWN, entity.getStatus());
        assertEquals(ConsentPurpose.ANALYTICS, entity.getConsentPurpose());
        assertEquals(ConsentType.OPTIONAL, entity.getConsentType());
    }
    
    @Test
    void testUniqueConstraintOnPersonAndStatement() {
        // Given
        Consent consent1 = Consent.builder()
            .personRef(testPersonRef)
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .purpose(ConsentPurpose.MARKETING)
            .type(ConsentType.ONE_TIME)
            .build();

        adapter.save(consent1);
        entityManager.flush(); // Ensure the first consent is persisted before trying to save the duplicate

        Consent duplicateConsent = Consent.builder()
            .personRef(testPersonRef)
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .purpose(ConsentPurpose.ANALYTICS)
            .type(ConsentType.ONE_TIME)
            .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            adapter.save(duplicateConsent); entityManager.flush();
        });
    }
     
    @Test
    void testMapperToDomainWithNullEntity() {
        // When
        Consent result = mapper.toDomain(null);

        // Then
        assertNull(result);
    }
    

    @Test
    void testMapperToEntityWithNullDomain() {
        // When
        ConsentEntity result = mapper.toEntity(null);

        // Then
        assertNull(result);
    }
     
    @Test
    void testMapperToEntityWithValidConsent() {
        // Given - Test med gyldig Consent (builder genererer automatisk id)
        Consent consent = Consent.builder()
            .id(null) // Builder vil generere id
            .personRef(testPersonRef)
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .purpose(ConsentPurpose.MARKETING)
            .type(ConsentType.ONE_TIME)
            .build();

        // When
        ConsentEntity result = mapper.toEntity(consent);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId()); // Id blev genereret af builder
        assertEquals(testPersonRef.value(), result.getPersonReference());
        assertEquals(testStatement.getId(), result.getConsentStatement().getId());
        assertEquals(ConsentStatus.ACTIVE, result.getStatus());
        assertEquals(ConsentPurpose.MARKETING, result.getConsentPurpose());
        assertEquals(ConsentType.ONE_TIME, result.getConsentType());
    }
     
    
    @Test
    void testStatementFactoryCalledInMapper() {
        // Given
        Consent consent = Consent.builder()
            .id(new ConsentId(UUID.randomUUID()))
            .personRef(testPersonRef)
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .purpose(ConsentPurpose.MARKETING)
            .type(ConsentType.OPTIONAL)
            .build();

        // When
        ConsentEntity result = mapper.toEntity(consent);

        // Then
        assertNotNull(result);
        assertEquals(testStatement, result.getConsentStatement());
    }

    
    @Test
    void testCompensate_WhenSagaStateIsWrong_ShouldThrowConflictException() {
        UUID id = UUID.randomUUID();

        assertThrows(ConflictException.class, () -> adapter.compensate(
                new ConsentId(id),
                SagaOutcome.IDEMPOTENT));
    }

    @Test
    void testCompensate_WhenConsentDoesNotExist_ShouldReturnFalse() {
        // Given
        UUID id = UUID.randomUUID();

        // When
        Boolean result = adapter.compensate(
                new ConsentId(id),
                SagaOutcome.COMPENSATE);

        // Then
        assertFalse(result);
    }

    @Test
    void testCompensate_WhenConsentExistsAndDeleteSucceeds_ShouldReturnTrue() {
        // Given
        Consent consent = Consent.builder()
                .personRef(testPersonRef)
                .consentStatementRef(testStatementRef)
                .status(ConsentStatus.ACTIVE)
                .purpose(ConsentPurpose.MARKETING)
                .type(ConsentType.ONE_TIME)
                .build();

        Consent saved = adapter.save(consent);

        // When
        Boolean result = adapter.compensate(
                saved.getId(),
                SagaOutcome.COMPENSATE);

        // Then
        assertTrue(result);

        // Verify DB state (should be deleted)
        ConsentEntity entity = entityManager.find(ConsentEntity.class, saved.getId().value());

        assertNull(entity);
    }

    @Test
    void testCompensate_WhenEntityDoesNotExist_ShouldReturnFalse() {
        // Given
        UUID id = UUID.randomUUID();

        // When
        Boolean result = adapter.compensate(
                new ConsentId(id),
                SagaOutcome.COMPENSATE);

        // Then
        assertFalse(result);
    }

    @Test
    void testFindByPersonAndPurposeWhenExists() {
        // Given
        Consent consent = Consent.builder()
                .personRef(testPersonRef)
                .consentStatementRef(testStatementRef)
                .status(ConsentStatus.ACTIVE)
                .purpose(ConsentPurpose.MARKETING)
                .type(ConsentType.ONE_TIME)
                .build();

        adapter.save(consent);

        // When
        Optional<Consent> result = adapter.findByPersonAndPurpose(
                testPersonRef,
                ConsentPurpose.MARKETING);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testPersonRef, result.get().getPersonRef());
        assertEquals(ConsentPurpose.MARKETING, result.get().getPurpose());
        assertEquals(ConsentStatus.ACTIVE, result.get().getStatus());
    }

    @Test
    void testFindByPersonAndPurposeWhenNotExists() {
        // When
        Optional<Consent> result = adapter.findByPersonAndPurpose(
                new PersonRef(UUID.randomUUID()),
                ConsentPurpose.MARKETING);

        // Then
        assertFalse(result.isPresent());
    }

}
