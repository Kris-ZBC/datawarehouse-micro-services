package local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.enums.ConsentType;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.consent.domain.model.Consent;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentId;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentStatementRef;
import local.sop.datawarehouse.consent.domain.model.valueobject.PersonRef;
import local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement.ConsentStatementEntity;
import local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement.factory.StatementEntityFactory;

@ExtendWith(MockitoExtension.class)
public class ConsentJpaMapperTest {

        @InjectMocks
    private ConsentJpaMapper mapper;
 
    @Mock
    private StatementEntityFactory statementFactory;
 
    private static final UUID CONSENT_ID = UUID.fromString("111e4567-e89b-12d3-a456-426614174111");
    private static final UUID PERSON_REF = UUID.fromString("222e4567-e89b-12d3-a456-426614174222");
    private static final UUID STATEMENT_ID = UUID.fromString("333e4567-e89b-12d3-a456-426614174333");
 
    // ========== ENTITY TO DOMAIN TESTS ==========
 
    @Test
    @DisplayName("Should map basic ConsentEntity to domain")
    void shouldMapBasicEntityToDomain() {
        // Given — CHANGED: purpose/type live on the statement now, not
        // on ConsentEntity/Consent.
        ConsentStatementEntity statementEntity = ConsentStatementEntity.builder()
                .id(STATEMENT_ID)
                .statementText("Marketing consent")
                .purpose(ConsentPurpose.MARKETING)
                .type(ConsentType.OPTIONAL)
                .active(true)
                .build();
 
        ConsentEntity entity = ConsentEntity.builder()
                .id(CONSENT_ID)
                .personReference(PERSON_REF)
                .consentStatement(statementEntity)
                .status(ConsentStatus.ACTIVE)
                .build();
 
        // When
        Consent result = mapper.toDomain(entity);
 
        // Then
        assertEquals(CONSENT_ID, result.getId().value());
        assertEquals(PERSON_REF, result.getPersonRef().value());
        assertEquals(STATEMENT_ID, result.getConsentStatementRef().value());
        assertEquals(ConsentStatus.ACTIVE, result.getStatus());
 
    }
 
     // ========== DOMAIN TO ENTITY TESTS ==========
 
    @Test
    @DisplayName("Should map basic domain to entity")
    void shouldMapBasicDomainToEntity() {
        // Given
        Consent domain = Consent.builder()
                .id(ConsentId.of(CONSENT_ID))
                .personRef(PersonRef.of(PERSON_REF))
                .consentStatementRef(ConsentStatementRef.of(STATEMENT_ID))
                .status(ConsentStatus.ACTIVE)
                .build();
 
        ConsentStatementEntity expectedStatement = ConsentStatementEntity.builder()
                .id(STATEMENT_ID)
                .statementText("test")
                .purpose(ConsentPurpose.MARKETING)
                .type(ConsentType.OPTIONAL)
                .active(true)
                .build();
 
        when(statementFactory.createStatementEntity(any(ConsentStatementRef.class)))
                .thenReturn(expectedStatement);
 
        // When
        ConsentEntity result = mapper.toEntity(domain);
 
        // Then
        assertEquals(CONSENT_ID, result.getId());
        assertEquals(PERSON_REF, result.getPersonReference());
        assertEquals(ConsentStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getConsentStatement());
        assertEquals(STATEMENT_ID, result.getConsentStatement().getId());
        assertEquals("test", result.getConsentStatement().getStatementText());
        verify(statementFactory).createStatementEntity(ConsentStatementRef.of(STATEMENT_ID));
    }
 
    @Test
    @DisplayName("Should handle null domain")
    void shouldHandleNullDomain() {
        // When
        ConsentEntity result = mapper.toEntity(null);
 
        // Then
        assertNull(result);
    }
 
    @Test
    @DisplayName("Should throw exception when domain has null ID")
    void shouldThrowExceptionWhenDomainHasNullId() {
        // Given
 
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> { 
                 mapper.toEntity(Consent.builder()
                .id(null) // Null ID caught by value object validation
                .personRef(PersonRef.of(PERSON_REF))
                .consentStatementRef(ConsentStatementRef.of(STATEMENT_ID))
                .status(ConsentStatus.ACTIVE)
                .build());
            });
        assertEquals("consent.consentstatement.invalid", exception.getMessage());
    }
 
 
    // ========== EDGE CASES ==========
 
    @Test
    @DisplayName("Should verify statement entity only has ID when mapped from domain")
    void shouldVerifyStatementEntityOnlyHasIdWhenMappedFromDomain() {
        // Given
        Consent domain = Consent.builder()
                .id(ConsentId.of(CONSENT_ID))
                .personRef(PersonRef.of(PERSON_REF))
                .consentStatementRef(ConsentStatementRef.of(STATEMENT_ID))
                .status(ConsentStatus.ACTIVE)
                .build();
 
 
         ConsentStatementEntity expectedStatement = ConsentStatementEntity.builder()
                .id(STATEMENT_ID)
                .statementText("test")
                .purpose(ConsentPurpose.MARKETING)
                .type(ConsentType.OPTIONAL)
                .active(true)
                .build();
 
        when(statementFactory.createStatementEntity(any(ConsentStatementRef.class)))
                .thenReturn(expectedStatement);
 
 
        // When
        ConsentEntity result = mapper.toEntity(domain);
 
        // Then
        assertNotNull(result.getConsentStatement());
        assertEquals(STATEMENT_ID, result.getConsentStatement().getId());
        assertEquals("test", result.getConsentStatement().getStatementText());
        assertTrue(result.getConsentStatement().isActive());
        assertTrue(result.getConsentStatement().getConsents().isEmpty()); // Should not be null
        verify(statementFactory).createStatementEntity(ConsentStatementRef.of(STATEMENT_ID));
    }
 
    @Test
    @DisplayName("Should handle all status values, statement carrying every purpose/type combination")
    void shouldHandleAllEnumCombinations() {
        // CHANGED: purpose/type are no longer axes of Consent itself, so
        // the combinatorial sweep now varies status (Consent's own
        // remaining enum) against every purpose/type combination on the
        // STATEMENT being referenced, rather than all three on Consent.
        for (ConsentStatus status : ConsentStatus.values()) {
            for (ConsentPurpose purpose : ConsentPurpose.values()) {
                for (ConsentType type : ConsentType.values()) {
                    // Given
                    Consent domain = Consent.builder()
                            .id(ConsentId.of(CONSENT_ID))
                            .personRef(PersonRef.of(PERSON_REF))
                            .consentStatementRef(ConsentStatementRef.of(STATEMENT_ID))
                            .status(status)
                            .build();
 
                    ConsentStatementEntity expectedStatement = ConsentStatementEntity.builder()
                        .id(STATEMENT_ID)
                        .statementText("test")
                        .purpose(purpose)
                        .type(type)
                        .active(true)
                    .build();
 
                    when(statementFactory.createStatementEntity(any(ConsentStatementRef.class)))
                        .thenReturn(expectedStatement);
 
 
                    // When
                    ConsentEntity result = mapper.toEntity(domain);
 
                    // Then
                    assertEquals(status, result.getStatus());
                    assertEquals(purpose, result.getConsentStatement().getPurpose());
                    assertEquals(type, result.getConsentStatement().getType());
                    verify(statementFactory).createStatementEntity(ConsentStatementRef.of(STATEMENT_ID));
                    reset(statementFactory);
                }
            }
        }
    }
}
