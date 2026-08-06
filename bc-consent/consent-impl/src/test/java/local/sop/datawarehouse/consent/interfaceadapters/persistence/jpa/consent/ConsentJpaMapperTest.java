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
        // Given
        ConsentStatementEntity statementEntity = ConsentStatementEntity.builder()
                .id(STATEMENT_ID)
                .statementText("Marketing consent")
                .active(true)
                .build();

        ConsentEntity entity = ConsentEntity.builder()
                .id(CONSENT_ID)
                .personReference(PERSON_REF)
                .consentStatement(statementEntity)
                .status(ConsentStatus.ACTIVE)
                .consentPurpose(ConsentPurpose.MARKETING)
                .consentType(ConsentType.OPTIONAL)
                .build();

        // When
        Consent result = mapper.toDomain(entity);

        // Then
        assertEquals(CONSENT_ID, result.getId().value());
        assertEquals(PERSON_REF, result.getPersonRef().value());
        assertEquals(STATEMENT_ID, result.getConsentStatementRef().value());
        assertEquals(ConsentStatus.ACTIVE, result.getStatus());
        assertEquals(ConsentPurpose.MARKETING, result.getPurpose());
        assertEquals(ConsentType.OPTIONAL, result.getType());

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
                .purpose(ConsentPurpose.MARKETING)
                .type(ConsentType.OPTIONAL)
                .build();

        ConsentStatementEntity expectedStatement = ConsentStatementEntity.builder()
                .id(STATEMENT_ID)
                .statementText("test")
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
        assertEquals(ConsentPurpose.MARKETING, result.getConsentPurpose());
        assertEquals(ConsentType.OPTIONAL, result.getConsentType());
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
                .purpose(ConsentPurpose.MARKETING)
                .type(ConsentType.OPTIONAL)
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
                .purpose(ConsentPurpose.MARKETING)
                .type(ConsentType.OPTIONAL)

                .build();


         ConsentStatementEntity expectedStatement = ConsentStatementEntity.builder()
                .id(STATEMENT_ID)
                .statementText("test")
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
    @DisplayName("Should handle all enum combinations")
    void shouldHandleAllEnumCombinations() {
        for (ConsentStatus status : ConsentStatus.values()) {
            for (ConsentPurpose purpose : ConsentPurpose.values()) {
                for (ConsentType type : ConsentType.values()) {
                    // Given
                    Consent domain = Consent.builder()
                            .id(ConsentId.of(CONSENT_ID))
                            .personRef(PersonRef.of(PERSON_REF))
                            .consentStatementRef(ConsentStatementRef.of(STATEMENT_ID))
                            .status(status)
                            .purpose(purpose)
                            .type(type)
                            .build();

                    ConsentStatementEntity expectedStatement = ConsentStatementEntity.builder()
                        .id(STATEMENT_ID)
                        .statementText("test")
                        .active(true)
                    .build();

                    when(statementFactory.createStatementEntity(any(ConsentStatementRef.class)))
                        .thenReturn(expectedStatement);


                    // When
                    ConsentEntity result = mapper.toEntity(domain);

                    // Then
                    assertEquals(status, result.getStatus());
                    assertEquals(purpose, result.getConsentPurpose());
                    assertEquals(type, result.getConsentType());
                    verify(statementFactory).createStatementEntity(ConsentStatementRef.of(STATEMENT_ID));
                    reset(statementFactory);
                }
            }
        }
    }
}
