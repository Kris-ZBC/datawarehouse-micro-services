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
 
    @Autowired 
    private StatementEntityFactory statementFactory;// use the real factory not the mockup
 
    // CHANGED: purpose/type moved to ConsentStatement (see main-source
    // changes). testStatement now carries MARKETING/ONE_TIME so tests
    // that filter by purpose/type still have something meaningful to
    // match against. Tests that previously varied purpose/type PER
    // CONSENT on the same statement have been restructured to use a
    // second statement instead — a Consent can no longer disagree with
    // its own statement about what purpose/type it serves.
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
                // CHANGED: look up an already-persisted statement first.
                // Several tests call persistStatement() to set up a
                // second statement BEFORE building a Consent against it
                // — this factory used to blindly persist a fresh entity
                // for any non-testStatementRef ref, which collided with
                // that pre-persisted row and threw EntityExistsException
                // ("a different object with the same identifier..."),
                // since Hibernate saw two distinct Java objects claiming
                // the same @Id. Only create+persist when nothing exists yet.
                return statementRepository.findById(statementRef.value())
                        .orElseGet(() -> {
                            ConsentStatementEntity newStatement = ConsentStatementEntity.builder()
                                .id(statementRef.value())
                                .statementText("test")
                                .purpose(ConsentPurpose.MARKETING)
                                .type(ConsentType.REQUIRED)
                                .active(true)
                                .build();
                            entityManager.persistAndFlush(newStatement);
                            return newStatement;
                        });
            }
        };
 
        mapper = new ConsentJpaMapper(statementFactory);
        adapter = new ConsentRepositoryJpaAdapter(repository, statementRepository, mapper);
 
        // create test statement FØR factory setup
        testStatement = ConsentStatementEntity.builder()
            .id(UUID.randomUUID())
            .statementText("Test statement")
            .purpose(ConsentPurpose.MARKETING)
            .type(ConsentType.ONE_TIME)
            .active(true)
            .build();
        entityManager.persistAndFlush(testStatement);
 
        testPersonRef = new PersonRef(UUID.randomUUID());
        testStatementRef = new ConsentStatementRef(testStatement.getId());
    }
 
    // Helper for the (now common) case of a second statement with its
    // own purpose/type, needed whenever a test wants two consents that
    // differ on those axes.
    private ConsentStatementEntity persistStatement(ConsentPurpose purpose, ConsentType type) {
        ConsentStatementEntity statement = ConsentStatementEntity.builder()
            .id(UUID.randomUUID())
            .statementText("Another statement")
            .purpose(purpose)
            .type(type)
            .active(true)
            .build();
        entityManager.persistAndFlush(statement);
        return statement;
    }
 
    @Test
    @Transactional
    void testGrantConsent_ShouldUpdateStatementAndSetConsent() {
        // Given
        Consent grantedConsent = Consent.builder()
            .personRef(testPersonRef)
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
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
        // Given - opret flere consents for samme statement. purpose/type
        // no longer varies per consent, so both just take the
        // statement's own MARKETING/ONE_TIME.
        Consent consent1 = Consent.builder()
            .personRef(new PersonRef(UUID.randomUUID()))
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .build();
 
        Consent consent2 = Consent.builder()
            .personRef(new PersonRef(UUID.randomUUID()))
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
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
            .build();
 
        Consent revokedConsent = Consent.builder()
            .personRef(new PersonRef(UUID.randomUUID()))
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.WITHDRAWN)
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
        // Given - consent2 already used a second statement, which is
        // exactly the pattern the other restructured tests now follow.
        Consent consent1 = Consent.builder()
            .personRef(testPersonRef)
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .build();
 
        ConsentStatementEntity statement2 = persistStatement(ConsentPurpose.ANALYTICS, ConsentType.OPTIONAL);
 
        Consent consent2 = Consent.builder()
            .personRef(testPersonRef)
            .consentStatementRef(new ConsentStatementRef(statement2.getId()))
            .status(ConsentStatus.WITHDRAWN)
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
        // Given — restructured: purpose/type now come from the
        // statement, so consent1/consent2 share testStatement
        // (MARKETING/ONE_TIME) and only differ by nothing relevant;
        // consent3 needs a DIFFERENT type, so it points at a second
        // statement (MARKETING/OPTIONAL) rather than claiming a
        // different type on the same statement, which is no longer
        // expressible.
        Consent consent1 = Consent.builder()
            .personRef(new PersonRef(UUID.randomUUID()))
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .build();
 
        Consent consent2 = Consent.builder()
            .personRef(new PersonRef(UUID.randomUUID()))
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .build();
 
        ConsentStatementEntity statement2 = persistStatement(ConsentPurpose.MARKETING, ConsentType.OPTIONAL);
        Consent consent3 = Consent.builder()
            .personRef(new PersonRef(UUID.randomUUID()))
            .consentStatementRef(new ConsentStatementRef(statement2.getId()))
            .status(ConsentStatus.WITHDRAWN)
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
            .build();
        adapter.save(consent1);
 
        ConsentStatementEntity statement2 = persistStatement(ConsentPurpose.ANALYTICS, ConsentType.OPTIONAL);
        Consent consent2 = Consent.builder()
            .personRef(new PersonRef(UUID.randomUUID()))
            .consentStatementRef(new ConsentStatementRef(statement2.getId()))
            .status(ConsentStatus.WITHDRAWN)
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
        // CHANGED: this test previously exercised withPurpose()/withType(),
        // which no longer exist on Consent — update() only ever touched
        // status anyway (see ConsentRepositoryJpaAdapter.update()), so
        // this now tests exactly that: status update, nothing else.
        Consent originalConsent = Consent.builder()
            .personRef(testPersonRef)
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .build();
 
        Consent savedConsent = adapter.save(originalConsent);
 
        // When
        Consent updatedConsent = savedConsent.withStatus(ConsentStatus.WITHDRAWN);
        Consent result = adapter.update(updatedConsent);
 
        // Then
        assertNotNull(result);
        assertEquals(savedConsent.getId(), result.getId());
        assertEquals(ConsentStatus.WITHDRAWN, result.getStatus());
 
        // Verify database state
        ConsentEntity entity = entityManager.find(ConsentEntity.class, result.getId().value());
        assertEquals(ConsentStatus.WITHDRAWN, entity.getStatus());
    }
 
    @Test
    void testUniqueConstraintOnPersonAndStatement() {
        // Given
        Consent consent1 = Consent.builder()
            .personRef(testPersonRef)
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
            .build();
 
        adapter.save(consent1);
        entityManager.flush(); // Ensure the first consent is persisted before trying to save the duplicate
 
        Consent duplicateConsent = Consent.builder()
            .personRef(testPersonRef)
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
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
            .build();
 
        // When
        ConsentEntity result = mapper.toEntity(consent);
 
        // Then
        assertNotNull(result);
        assertNotNull(result.getId()); // Id blev genereret af builder
        assertEquals(testPersonRef.value(), result.getPersonReference());
        assertEquals(testStatement.getId(), result.getConsentStatement().getId());
        assertEquals(ConsentStatus.ACTIVE, result.getStatus());
    }
 
 
    @Test
    void testStatementFactoryCalledInMapper() {
        // Given
        Consent consent = Consent.builder()
            .id(new ConsentId(UUID.randomUUID()))
            .personRef(testPersonRef)
            .consentStatementRef(testStatementRef)
            .status(ConsentStatus.ACTIVE)
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
        // Given - purpose comes from testStatement (MARKETING) now, not
        // from the consent itself.
        Consent consent = Consent.builder()
                .personRef(testPersonRef)
                .consentStatementRef(testStatementRef)
                .status(ConsentStatus.ACTIVE)
                .build();
 
        adapter.save(consent);
 
        // When
        Optional<Consent> result = adapter.findByPersonAndPurpose(
                testPersonRef,
                ConsentPurpose.MARKETING);
 
        // Then
        assertTrue(result.isPresent());
        assertEquals(testPersonRef, result.get().getPersonRef());
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
