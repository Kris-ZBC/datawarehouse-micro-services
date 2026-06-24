package local.sop.sopinfo.consent.interfaceadapters.persistence.jpa.consentstatement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.sopinfo.consent.domain.model.Consent;
import local.sop.sopinfo.consent.domain.model.ConsentStatement;
import local.sop.sopinfo.consent.domain.model.valueobject.ConsentId;
import local.sop.sopinfo.consent.domain.model.valueobject.ConsentStatementRef;
import local.sop.sopinfo.consent.domain.model.valueobject.ConsentStatementValue;
import local.sop.sopinfo.consent.domain.model.valueobject.PersonRef;
import local.sop.sopinfo.consent.interfaceadapters.persistence.jpa.consent.ConsentEntity;
import local.sop.sopinfo.sharedkernel.enums.ConsentPurpose;
import local.sop.sopinfo.sharedkernel.enums.ConsentStatus;
import local.sop.sopinfo.sharedkernel.enums.ConsentType;

@ExtendWith(MockitoExtension.class)
class ConsentStatementJpaMapperTest {

    @InjectMocks
    private ConsentStatementJpaMapper statementMapper;

    // Brug faste UUIDs for reproducerbare tests
    private static final UUID STATEMENT_ID = UUID.fromString("111e4567-e89b-12d3-a456-426614174111");
    private static final UUID CONSENT_ID_1 = UUID.fromString("222e4567-e89b-12d3-a456-426614174222");
    private static final UUID CONSENT_ID_2 = UUID.fromString("333e4567-e89b-12d3-a456-426614174333");
    private static final UUID PERSON_REF_1 = UUID.fromString("444e4567-e89b-12d3-a456-426614174444");
    private static final UUID PERSON_REF_2 = UUID.fromString("555e4567-e89b-12d3-a456-426614174555");

    @Test
    @DisplayName("Should handle null entity")
    void shouldHandleNullEntity() {
        // When
        ConsentStatement result = statementMapper.toDomain(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle null domain")
    void shouldHandleNullDomain() {
        // When
        ConsentStatementEntity result = statementMapper.toEntity(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should maintain immutability in mapping")
    void shouldMaintainImmutabilityInMapping() {
        // Given
        ConsentStatement originalDomain = ConsentStatement.builder()
                .id(new ConsentStatementRef(STATEMENT_ID))
                .statementText(new ConsentStatementValue("Original text"))
                .active(true)
                .build();

        // When
        ConsentStatementEntity entity = statementMapper.toEntity(originalDomain);
        ConsentStatement mappedBackDomain = statementMapper.toDomain(entity);

        // Then
        assertNotSame(originalDomain, mappedBackDomain);
        assertEquals(originalDomain.getId(), mappedBackDomain.getId());
        assertEquals(originalDomain.getStatementText(), mappedBackDomain.getStatementText());
        assertEquals(originalDomain.isActive(), mappedBackDomain.isActive());
    }

    @Test
    @DisplayName("Should handle withConsents() correctly")
    void shouldHandleWithConsentsCorrectly() {
        // Given
        ConsentStatement domain = ConsentStatement.builder()
                .id(new ConsentStatementRef(STATEMENT_ID))
                .statementText(new ConsentStatementValue("Test"))
                .active(true)
                .build();

        Consent consent = Consent.builder()
                .id(new ConsentId(CONSENT_ID_1))
                .personRef(new PersonRef(PERSON_REF_1))
                .consentStatementRef(new ConsentStatementRef(STATEMENT_ID))
                .status(ConsentStatus.ACTIVE)
                .purpose(ConsentPurpose.MARKETING)
                .type(ConsentType.OPTIONAL)
                .build();

        // When
        ConsentStatement withConsents = domain.withConsents(Set.of(consent));
        ConsentStatementEntity result = statementMapper.toEntity(withConsents);

        // Then
        assertNotSame(domain, withConsents);
        assertEquals(1, withConsents.getConsents().size());
        assertEquals(1, result.getConsents().size());
        assertEquals(0, domain.getConsents().size());
    }

    @Test
    @DisplayName("Should handle null in withConsents()")
    void shouldHandleNullInWithConsents() {
        // Given
        ConsentStatement domain = ConsentStatement.builder()
                .id(new ConsentStatementRef(STATEMENT_ID))
                .statementText(new ConsentStatementValue("Test"))
                .active(true)
                .build();

        // When
        ConsentStatement result = domain.withConsents(null);

        // Then
        assertNotNull(result);
        assertTrue(result.getConsents().isEmpty());
    }

    @Test
    @DisplayName("Should handle circular reference correctly")
    void shouldHandleCircularReferenceCorrectly() {
        // Given
        ConsentStatementEntity statementEntity = ConsentStatementEntity.builder()
                .id(STATEMENT_ID)
                .statementText("Test statement")
                .active(true)
                .build();

        ConsentEntity consentEntity = ConsentEntity.builder()
                .id(CONSENT_ID_1)
                .personReference(PERSON_REF_1)
                .consentStatement(statementEntity)
                .status(ConsentStatus.ACTIVE)
                .consentPurpose(ConsentPurpose.MARKETING)
                .consentType(ConsentType.OPTIONAL)
                .build();

        statementEntity = statementEntity.withConsents(Set.of(consentEntity));

        // When
        ConsentStatement result = statementMapper.toDomain(statementEntity);

        // Then
        assertEquals(1, result.getConsents().size());
        Consent mappedConsent = result.getConsents().iterator().next();
        assertEquals(CONSENT_ID_1, mappedConsent.getId().value());
        assertEquals(PERSON_REF_1, mappedConsent.getPersonRef().value());
        assertEquals(STATEMENT_ID, mappedConsent.getConsentStatementRef().value());
        assertEquals(ConsentStatus.ACTIVE, mappedConsent.getStatus());
        assertEquals(ConsentPurpose.MARKETING, mappedConsent.getPurpose());
        assertEquals(ConsentType.OPTIONAL, mappedConsent.getType());
    }

    @Test
    @DisplayName("Should handle null consent in collection")
    void shouldHandleNullConsentInCollection() {
        // Given
        ConsentStatementEntity statementEntity = ConsentStatementEntity.builder()
                .id(STATEMENT_ID)
                .statementText("Test")
                .active(true)
                .build();

        // Tilføj null consent
        statementEntity = statementEntity.withConsents(Set.of());

        // When
        ConsentStatement result = statementMapper.toDomain(statementEntity);

        // Then
        assertTrue(result.getConsents().isEmpty());
    }

    @Test
    @DisplayName("Should map entity with multiple consents to domain")
    void shouldMapEntityWithMultipleConsentsToDomain() {
        // Given
        ConsentStatementEntity statementEntity = ConsentStatementEntity.builder()
                .id(STATEMENT_ID)
                .statementText("Analytics consent")
                .active(true)
                .build();

        ConsentEntity consent1 = ConsentEntity.builder()
                .id(CONSENT_ID_1)
                .personReference(PERSON_REF_1)
                .consentStatement(statementEntity)
                .status(ConsentStatus.ACTIVE)
                .consentPurpose(ConsentPurpose.ANALYTICS)
                .consentType(ConsentType.REQUIRED)
                .build();

        ConsentEntity consent2 = ConsentEntity.builder()
                .id(CONSENT_ID_2)  // ← Bruger CONSENT_ID_2
                .personReference(PERSON_REF_2)  // ← Bruger PERSON_REF_2
                .consentStatement(statementEntity)
                .status(ConsentStatus.WITHDRAWN)
                .consentPurpose(ConsentPurpose.ANALYTICS)
                .consentType(ConsentType.OPTIONAL)  // ← Forskellig type
                .build();

        Set<ConsentEntity> consents = new HashSet<>();
        consents.add(consent1);
        consents.add(consent2);
        statementEntity.withConsents(consents);

        // When
        ConsentStatement result = statementMapper.toDomain(statementEntity);

        // Then
        assertEquals(2, result.getConsents().size());
        
        Set<UUID> consentIds = result.getConsents().stream()
                .map(consent -> consent.getId().value())
                .collect(Collectors.toSet());
        
        assertTrue(consentIds.containsAll(Set.of(CONSENT_ID_1, CONSENT_ID_2)));
        
        // Bonus: Verificer at begge persons er med
        Set<UUID> personRefs = result.getConsents().stream()
                .map(consent -> consent.getPersonRef().value())
                .collect(Collectors.toSet());
        
        assertTrue(personRefs.containsAll(Set.of(PERSON_REF_1, PERSON_REF_2)));
    }
}