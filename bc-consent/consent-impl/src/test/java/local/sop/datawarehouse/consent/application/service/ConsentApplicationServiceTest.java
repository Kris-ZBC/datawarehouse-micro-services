package local.sop.datawarehouse.consent.application.service;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.enums.ConsentType;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;
import local.sop.datawarehouse.consent.application.api.dto.*;
import local.sop.datawarehouse.consent.domain.model.Consent;
import local.sop.datawarehouse.consent.domain.model.ConsentStatement;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentId;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentStatementRef;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentStatementValue;
import local.sop.datawarehouse.consent.domain.model.valueobject.PersonRef;
import local.sop.datawarehouse.consent.domain.ports.out.ConsentRepositoryPort;
import local.sop.datawarehouse.consent.domain.ports.out.ConsentStatementRepositoryPort;


@ExtendWith(MockitoExtension.class)
class ConsentApplicationServiceTest {

    @Mock
    private ConsentRepositoryPort consentRepositoryPort;

    @Mock
    private ConsentStatementRepositoryPort consentStatementRepository;

    @InjectMocks
    private ConsentApplicationService service;

    private ConsentStatement testStatement;
    private Consent testConsent;
    private UUID statementId;
    private UUID consentId;
    private UUID personId;

    @BeforeEach
    void setUp() {
        statementId = UUID.randomUUID();
        consentId = UUID.randomUUID();
        personId = UUID.randomUUID();
        testStatement = ConsentStatement.builder()
            .id(new ConsentStatementRef(statementId))
            .statementText(new ConsentStatementValue("Test statement"))
            .active(false)
            .build();
        testConsent = Consent.builder()
            .id(new ConsentId(consentId))
            .personRef(PersonRef.of(personId))
            .consentStatementRef(new ConsentStatementRef(statementId))
            .status(ConsentStatus.ACTIVE)
            .purpose(ConsentPurpose.MARKETING)
            .type(ConsentType.ONE_TIME)
            .build();
    }

    @Test
    void testCreateConsentStatement_ShouldCreateAndReturnResponse() {
        // Given
        CreateConsentStatementCmd cmd = new CreateConsentStatementCmd(true, "New statement");
        ConsentStatement expectedStatement = ConsentStatement.builder()
            .statementText(new ConsentStatementValue("New statement"))
            .active(true)
            .build();

        when(consentStatementRepository.save(any(ConsentStatement.class)))
            .thenReturn(expectedStatement);

        // When
        ConsentStatementResponse response = service.createConsentStatement(cmd);

        // Then
        assertNotNull(response);
        assertEquals("New statement", response.statementText());
        assertTrue(response.active());
        verify(consentStatementRepository).save(argThat(statement -> 
            statement.getStatementText().equals("New statement") && 
            statement.isActive()
        ));
    }

    @Test
    void testGrantConsent_WithInactiveStatement_ShouldActivateStatementAndCreateConsent() {
        // Given
        GrantConsentCmd cmd = new GrantConsentCmd(
            personId,
            statementId,
            ConsentPurpose.MARKETING,
            ConsentType.ONE_TIME,
            ConsentStatus.ACTIVE
        );

        // Mock statement lookup - inactive
        when(consentStatementRepository.findById(ConsentStatementRef.of(statementId)))
            .thenReturn(Optional.of(testStatement));

        // Mock statement save (ikke updateActiveStatus!)
        ConsentStatement activeStatement = testStatement.withActive(true);
        ConsentStatement statementWithConsents = activeStatement.withConsents(Set.of(testConsent));
        when(consentStatementRepository.save(any(ConsentStatement.class)))
            .thenReturn(statementWithConsents);

        // Mock consent save
        when(consentRepositoryPort.save(any(Consent.class)))
            .thenReturn(testConsent);
        // When
        ConsentResponse response = service.grantConsent(cmd);

        // Then
        assertNotNull(response);
        assertEquals(consentId, response.consentId());
        assertEquals(personId, response.personReference());
        assertEquals("ACTIVE", response.status());
        assertEquals(statementId, response.consentStatementId());
        assertEquals("Test statement", response.consentStatementText());
        assertEquals("MARKETING", response.consentPurpose());
        assertEquals("ONE_TIME", response.consentType());
        assertTrue(response.active());

        // Verify interactions 
        verify(consentStatementRepository).save(any(ConsentStatement.class));
        verify(consentRepositoryPort).save(any(Consent.class));
    }

        @Test
    void testGrantConsent_WithActiveStatement_ShouldNotUpdateActiveStatus() {
        // Given
        ConsentStatement activeStatement = testStatement.withActive(true);
        GrantConsentCmd cmd = new GrantConsentCmd(
            personId,
            statementId,
            ConsentPurpose.MARKETING,
            ConsentType.ONE_TIME,
            ConsentStatus.ACTIVE
        );

        // Mock statement lookup - already active
        when(consentStatementRepository.findById(ConsentStatementRef.of(statementId)))
            .thenReturn(Optional.of(activeStatement));

        // Mock statement save
        ConsentStatement statementWithConsents = activeStatement.withConsents(Set.of(testConsent));
        when(consentStatementRepository.save(any(ConsentStatement.class)))
            .thenReturn(statementWithConsents);

        // Mock consent save
        when(consentRepositoryPort.save(any(Consent.class)))
            .thenReturn(testConsent);

        // When
        ConsentResponse response = service.grantConsent(cmd);

        // Then
        assertNotNull(response);
        assertEquals(consentId, response.consentId());
        assertEquals(personId, response.personReference());
        assertEquals("ACTIVE", response.status());
        assertEquals(statementId, response.consentStatementId());
        assertEquals("Test statement", response.consentStatementText());
        assertEquals("MARKETING", response.consentPurpose());
        assertEquals("ONE_TIME", response.consentType());
        assertTrue(response.active());

        // Verify that save was called but statement was already active
        verify(consentStatementRepository).save(any(ConsentStatement.class));
        verify(consentRepositoryPort).save(any(Consent.class));
    }

        @Test
    void testGrantConsent_WithNonExistentStatement_ShouldThrowNotFoundException() {
        // Given - statement findes ikke
        GrantConsentCmd cmd = new GrantConsentCmd(
            personId,
            statementId,
            ConsentPurpose.MARKETING,
            ConsentType.ONE_TIME,
            ConsentStatus.ACTIVE
        );

        // Mock statement lookup - returner empty
        when(consentStatementRepository.findById(ConsentStatementRef.of(statementId)))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> {
            service.grantConsent(cmd);
        });

        // Verify at ingen consent blev gemt
        verify(consentRepositoryPort, never()).save(any(Consent.class));
        verify(consentStatementRepository, never()).save(any(ConsentStatement.class));
    }

    @Test
    void testGetConsent_ShouldReturnConsentResponse() {
        // Given
        // Mock consent find
        when(consentRepositoryPort.findById(ConsentId.of(consentId)))
            .thenReturn(Optional.of(testConsent));
        
        // Mock statement find
        when(consentStatementRepository.findById(ConsentStatementRef.of(statementId)))
            .thenReturn(Optional.of(testStatement));

        // When
        Optional<ConsentResponse> response = service.getConsent(consentId);

        // Then
        assertTrue(response.isPresent());
        ConsentResponse consentResponse = response.get();
        assertEquals(consentId, consentResponse.consentId());
        assertEquals(personId, consentResponse.personReference());
        assertEquals("ACTIVE", consentResponse.status());
        assertEquals(statementId, response.get().consentStatementId());
        assertEquals("Test statement", consentResponse.consentStatementText());
        assertEquals("MARKETING", consentResponse.consentPurpose());
        assertEquals("ONE_TIME", consentResponse.consentType());
        assertFalse(response.get().active());

        // Verify interactions
        verify(consentRepositoryPort).findById(ConsentId.of(consentId));
        verify(consentStatementRepository).findById(ConsentStatementRef.of(statementId));
    }

    @Test
    void testGetConsent_WithNonExistentConsent_ShouldThrowNotFoundException() {
        // Given

        // Mock consent find - returner empty
        when(consentRepositoryPort.findById(ConsentId.of(consentId)))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> {
            service.getConsent(consentId);
        });

        // Verify at statement ikke blev søgt
        verify(consentStatementRepository, never()).findById(any(ConsentStatementRef.class));
    }

        @Test
    void testWithdrawConsent_ShouldUpdateStatusAndReturnResponse() {
        // Given
        RevokeConsentCmd cmd = new RevokeConsentCmd(consentId);
        Consent withdrawnConsent = testConsent.withStatus(ConsentStatus.WITHDRAWN);

        // Mock consent find
        when(consentRepositoryPort.findById(ConsentId.of(consentId)))
            .thenReturn(Optional.of(testConsent));
        
        // Mock consent update
        when(consentRepositoryPort.update(any(Consent.class)))
            .thenReturn(withdrawnConsent);
        
        // Mock statement find
        when(consentStatementRepository.findById(ConsentStatementRef.of(statementId)))
            .thenReturn(Optional.of(testStatement));

        // When
        ConsentResponse response = service.withdrawConsent(cmd);

        // Then
        assertNotNull(response);
        assertEquals(consentId, response.consentId());
        assertEquals(personId, response.personReference());
        assertEquals("WITHDRAWN", response.status());
        assertEquals(statementId, response.consentStatementId());
        assertEquals("Test statement", response.consentStatementText());
        assertEquals("MARKETING", response.consentPurpose());
        assertEquals("ONE_TIME", response.consentType());
        assertFalse(response.active());

        // Verify interactions
        verify(consentRepositoryPort).findById(ConsentId.of(consentId));
        verify(consentRepositoryPort).update(argThat(consent -> 
            consent.getStatus() == ConsentStatus.WITHDRAWN
        ));
        verify(consentStatementRepository).findById(ConsentStatementRef.of(statementId));
    }

    @Test
    void testWithdrawConsent_WithNonExistentConsent_ShouldThrowNotFoundException() {
        // Given
        RevokeConsentCmd cmd = new RevokeConsentCmd(consentId);

        // Mock consent find - returner empty
        when(consentRepositoryPort.findById(ConsentId.of(consentId)))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> {
            service.withdrawConsent(cmd);
        });

        // Verify at ingen update eller statement søgning skete
        verify(consentRepositoryPort, never()).update(any(Consent.class));
        verify(consentStatementRepository, never()).findById(any(ConsentStatementRef.class));
    }

    @Test
    void testUpdateConsentStatement_ShouldUpdateAndReturnResponse() {
        // Given - bemærk rækkefølge: consentStatementId, statementText, consentPurpose, consentType
        UpdateConsentStatementCmd cmd = new UpdateConsentStatementCmd(
            statementId,
            "Updated statement text",
            null, // consentPurpose - optional
            null  // consentType - optional
        );

        // Mock statement find
        when(consentStatementRepository.findById(ConsentStatementRef.of(statementId)))
            .thenReturn(Optional.of(testStatement));
        
        // Mock statement update
        ConsentStatement updatedStatement = testStatement
            .withStatementText(new ConsentStatementValue("Updated statement text"));
        
        when(consentStatementRepository.updateStatement(any(DomainId.class), anyBoolean(), anyString()))
            .thenReturn(updatedStatement);

        // When
        ConsentStatementResponse response = service.updateConsentStatement(cmd);

        // Then
        assertNotNull(response);
        assertEquals("Updated statement text", response.statementText());
        assertEquals(statementId, response.consentStatementId());
        assertFalse(response.active()); // testStatement er inaktiv

        // Verify interactions
        verify(consentStatementRepository).findById(ConsentStatementRef.of(statementId));
        verify(consentStatementRepository).updateStatement(
            eq(ConsentStatementRef.of(statementId)),
            eq(false), // original active status
            eq("Updated statement text")
        );
    }

    @Test
    void testGetConsentStatement_ShouldReturnStatementResponse() {
        // Given
        FetchConsentStatementQuery query = new FetchConsentStatementQuery(statementId);

        // Mock statement find
        when(consentStatementRepository.findById(ConsentStatementRef.of(statementId)))
            .thenReturn(Optional.of(testStatement));

        // When
        Optional<ConsentStatementResponse> response = service.getConsentStatement(query);

        // Then
        assertTrue(response.isPresent());
        ConsentStatementResponse statementResponse = response.get();
        assertEquals(statementId, statementResponse.consentStatementId());
        assertEquals("Test statement", statementResponse.statementText());
        assertFalse(statementResponse.active()); // testStatement er inaktiv

        // Verify interaction
        verify(consentStatementRepository).findById(ConsentStatementRef.of(statementId));
    }

    @Test
    void testGetAllConsents_ShouldReturnListOfConsentResponses() {
        // Given
        Consent consent2 = Consent.builder()
            .id(new ConsentId(UUID.randomUUID()))
            .personRef(PersonRef.of(UUID.randomUUID()))
            .consentStatementRef(new ConsentStatementRef(UUID.randomUUID()))
            .status(ConsentStatus.WITHDRAWN)
            .purpose(ConsentPurpose.ANALYTICS)
            .type(ConsentType.OPTIONAL)
            .build();

        ConsentStatement statement2 = ConsentStatement.builder()
            .id(new ConsentStatementRef(UUID.randomUUID()))
            .statementText(new ConsentStatementValue("Other statement"))
            .active(true)
            .build();

        // Mock consent find
        when(consentRepositoryPort.findAll())
            .thenReturn(List.of(testConsent, consent2));
        
        // Mock statement finds
        when(consentStatementRepository.findById(ConsentStatementRef.of(statementId)))
            .thenReturn(Optional.of(testStatement));
        when(consentStatementRepository.findById(ConsentStatementRef.of(consent2.getConsentStatementRef().value())))
            .thenReturn(Optional.of(statement2));

        // When
        List<ConsentResponse> responses = service.getAllConsents();

        // Then
        assertEquals(2, responses.size());
        
        ConsentResponse response1 = responses.get(0);
        assertEquals(consentId, response1.consentId());
        assertEquals("ACTIVE", response1.status());
        assertEquals("Test statement", response1.consentStatementText());
        assertFalse(response1.active());
        ConsentResponse response2 = responses.get(1);
                assertEquals("WITHDRAWN", response2.status());
        assertEquals("Other statement", response2.consentStatementText());
        assertTrue(response2.active());

        // Verify interactions
        verify(consentRepositoryPort).findAll();
        verify(consentStatementRepository, times(2)).findById(any(ConsentStatementRef.class));
    }

    @Test
    void testGetAllConsentStatements_ShouldReturnAllStatements() {
        // Given
        ConsentStatement statement2 = ConsentStatement.builder()
            .id(new ConsentStatementRef(UUID.randomUUID()))
            .statementText(new ConsentStatementValue("Another statement"))
            .active(true)
            .build();

        // Mock statement find
        when(consentStatementRepository.findAll())
            .thenReturn(List.of(testStatement, statement2));

        // When
        List<ConsentStatementResponse> responses = service.getAllConsentStatements();

        // Then
        assertEquals(2, responses.size());
        
        ConsentStatementResponse response1 = responses.get(0);
        assertEquals(statementId, response1.consentStatementId());
        assertEquals("Test statement", response1.statementText());
        assertFalse(response1.active());
        
        ConsentStatementResponse response2 = responses.get(1);
        assertEquals("Another statement", response2.statementText());
        assertTrue(response2.active());

        // Verify interaction
        verify(consentStatementRepository).findAll();
    }

    @Test
    void testGetAllConsentsForPerson_ShouldReturnPersonConsents() {
        // Given
        FetchAllConsentsForPersonQuery query = new FetchAllConsentsForPersonQuery(personId);

        // Mock consent find for person
        when(consentRepositoryPort.findByPersonReference(PersonRef.of(personId)))
            .thenReturn(List.of(testConsent));
        
        // Mock statement find
        when(consentStatementRepository.findById(ConsentStatementRef.of(statementId)))
            .thenReturn(Optional.of(testStatement));

        // When
        List<ConsentResponse> responses = service.getAllConsentsForPerson(query);

        // Then
        assertEquals(1, responses.size());
        
        ConsentResponse response = responses.get(0);
        assertEquals(consentId, response.consentId());
        assertEquals(personId, response.personReference());
        assertEquals("ACTIVE", response.status());
        assertEquals(statementId, response.consentStatementId());
        assertEquals("Test statement", response.consentStatementText());
        assertEquals("MARKETING", response.consentPurpose());
        assertEquals("ONE_TIME", response.consentType());
        assertFalse(response.active());

        // Verify interactions
        verify(consentRepositoryPort).findByPersonReference(PersonRef.of(personId));
        verify(consentStatementRepository).findById(ConsentStatementRef.of(statementId));
    }

    @Test
    void compensateConsentStatement_WhenStatementExistsAndCompensateSucceeds_ShouldReturnCompensated() {
        // Given
        UUID statementId = UUID.randomUUID();
        CompensateConsentStatementCmd cmd = new CompensateConsentStatementCmd(
            String.class, 
            SagaOutcome.COMPENSATE
        );
        
        when(consentStatementRepository.findById(ConsentStatementRef.of(statementId)))
            .thenReturn(Optional.of(testStatement));
        when(consentStatementRepository.compensate(ConsentStatementRef.of(statementId), cmd.sagaState()))
            .thenReturn(true);

        // When
        ResponseCompensated result = service.compensate(statementId, cmd.clazz(), cmd.sagaState());

        // Then
        assertEquals(SagaOutcome.COMPENSATED, result.sagaState());
        assertTrue(result.success());
        
        verify(consentStatementRepository).findById(ConsentStatementRef.of(statementId));
        verify(consentStatementRepository).compensate(ConsentStatementRef.of(statementId), cmd.sagaState());
    }

    @Test
    void compensateConsentStatement_WhenStatementDoesNotExist_ShouldReturnIdempotentFalse() {
        // Given
        UUID statementId = UUID.randomUUID();
        CompensateConsentStatementCmd cmd = new CompensateConsentStatementCmd(
            String.class, 
            SagaOutcome.COMPENSATE
        );
        
        when(consentStatementRepository.findById(ConsentStatementRef.of(statementId)))
            .thenReturn(null);

        // When
        ResponseCompensated result = service.compensate(statementId, cmd.clazz(), cmd.sagaState());

        // Then
        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertFalse(result.success());
        
        verify(consentStatementRepository).findById(ConsentStatementRef.of(statementId));
        verify(consentStatementRepository, never()).compensate(any(), any());
    }


    @Test
    void compensateConsentStatement_WhenCompensateFails_ShouldReturnIdempotentTrue() {
        // Given
        UUID statementId = UUID.randomUUID();
        CompensateConsentStatementCmd cmd = new CompensateConsentStatementCmd(
            String.class, 
            SagaOutcome.COMPENSATE
        );
        
        when(consentStatementRepository.findById(ConsentStatementRef.of(statementId)))
            .thenReturn(Optional.of(testStatement));
        when(consentStatementRepository.compensate(ConsentStatementRef.of(statementId), cmd.sagaState()))
            .thenReturn(false);

        // When
        ResponseCompensated result = service.compensate(statementId, cmd.clazz(), cmd.sagaState());

        // Then
        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertTrue(result.success());
        
        verify(consentStatementRepository).findById(ConsentStatementRef.of(statementId));
        verify(consentStatementRepository).compensate(ConsentStatementRef.of(statementId), cmd.sagaState());
    }

    @Test
    void testGetConsentForPersonAndPurpose_ShouldReturnConsentResponse() {

        // Given

        FetchConsentForPersonAndPurposeQuery query = new FetchConsentForPersonAndPurposeQuery(
                personId,
                ConsentPurpose.MARKETING);

        when(consentRepositoryPort.findByPersonAndPurpose(
                PersonRef.of(personId),
                ConsentPurpose.MARKETING))
                .thenReturn(Optional.of(testConsent));

        when(consentStatementRepository.findById(
                ConsentStatementRef.of(statementId)))
                .thenReturn(Optional.of(testStatement));

        // When
        Optional<ConsentResponse> result = service.getConsentForPersonAndPurpose(query);

        // Then
        assertTrue(result.isPresent());

        ConsentResponse response = result.get();

        assertEquals(consentId, response.consentId());
        assertEquals(personId, response.personReference());
        assertEquals("ACTIVE", response.status());
        assertEquals(statementId, response.consentStatementId());
        assertEquals("Test statement", response.consentStatementText());
        assertEquals("MARKETING", response.consentPurpose());
        assertEquals("ONE_TIME", response.consentType());
        assertEquals(false, response.active());
    }

    @Test
    void testGetConsentForPersonAndPurpose_WhenConsentNotFound_ShouldThrowNotFoundException() {

        // Given

        var query = new FetchConsentForPersonAndPurposeQuery(
                personId,
                ConsentPurpose.MARKETING);

        when(consentRepositoryPort.findByPersonAndPurpose(
                PersonRef.of(personId),
                ConsentPurpose.MARKETING))
                .thenReturn(Optional.empty());

        // When and Then
        assertThrows(NotFoundException.class,
                () -> service.getConsentForPersonAndPurpose(query));

    }

    @Test
    void testGetConsentForPersonAndPurpose_WhenStatementNotFound_ShouldThrowNotFoundException() {
        // Given
        var query = new FetchConsentForPersonAndPurposeQuery(
                personId,
                ConsentPurpose.MARKETING);

        when(consentRepositoryPort.findByPersonAndPurpose(
                PersonRef.of(personId),
                ConsentPurpose.MARKETING))
                .thenReturn(Optional.of(testConsent));

        when(consentStatementRepository.findById(
                ConsentStatementRef.of(statementId)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class,
                () -> service.getConsentForPersonAndPurpose(query));
    }

   
    @Test
    void compensateConsent_WhenConsentExistsAndCompensateSucceeds_ShouldReturnCompensated() {
        // Given
        UUID id = UUID.randomUUID();

        when(consentRepositoryPort.findById(ConsentId.of(id)))
                .thenReturn(Optional.of(testConsent));

        when(consentRepositoryPort.compensate(ConsentId.of(id), SagaOutcome.COMPENSATE))
                .thenReturn(true);

        // When
        ResponseCompensated result = service.compensateConsent(id, String.class, SagaOutcome.COMPENSATE);

        // Then
        assertEquals(SagaOutcome.COMPENSATED, result.sagaState());
        assertTrue(result.success());

        verify(consentRepositoryPort).findById(ConsentId.of(id));
        verify(consentRepositoryPort).compensate(ConsentId.of(id), SagaOutcome.COMPENSATE);
    }

    @Test
    void compensateConsent_WhenConsentDoesNotExist_ShouldReturnIdempotentFalse() {
        // Given
        UUID id = UUID.randomUUID();

        when(consentRepositoryPort.findById(ConsentId.of(id)))
                .thenReturn(null);

        // When
        ResponseCompensated result = service.compensateConsent(id, String.class, SagaOutcome.COMPENSATE);

        // Then
        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertFalse(result.success());

        verify(consentRepositoryPort).findById(ConsentId.of(id));
        verify(consentRepositoryPort, never())
                .compensate(any(), any());
    }

    @Test
    void compensateConsent_WhenCompensateFails_ShouldReturnIdempotentTrue() {
        // Given
        UUID id = UUID.randomUUID();

        when(consentRepositoryPort.findById(ConsentId.of(id)))
                .thenReturn(Optional.of(testConsent));

        when(consentRepositoryPort.compensate(ConsentId.of(id), SagaOutcome.COMPENSATE))
                .thenReturn(false);

        // When
        ResponseCompensated result = service.compensateConsent(id, String.class, SagaOutcome.COMPENSATE);

        // Then
        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertTrue(result.success());

        verify(consentRepositoryPort).findById(ConsentId.of(id));
        verify(consentRepositoryPort).compensate(ConsentId.of(id), SagaOutcome.COMPENSATE);
    }

    @Test
    void compensateConsentWithdrawal_WhenConsentNotFound_ShouldReturnIdempotentFalse() {
        // Given
        UUID id = UUID.randomUUID();

        when(consentRepositoryPort.findById(ConsentId.of(id)))
                .thenReturn(Optional.empty());

        // When
        ResponseCompensated result = service.compensateConsentWithdrawalUpdate(id, String.class,
                SagaOutcome.COMPENSATE);

        // Then
        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertFalse(result.success());

        verify(consentRepositoryPort).findById(ConsentId.of(id));
        verify(consentRepositoryPort, never()).update(any());
    }

    @Test
    void compensateConsentWithdrawal_WhenConsentNotWithdrawn_ShouldReturnIdempotentTrue() {
        // Given
        UUID id = UUID.randomUUID();

        Consent consent = testConsent.withStatus(ConsentStatus.ACTIVE);

        when(consentRepositoryPort.findById(ConsentId.of(id)))
                .thenReturn(Optional.of(consent));

        // When
        ResponseCompensated result = service.compensateConsentWithdrawalUpdate(id, String.class,
                SagaOutcome.COMPENSATE);

        // Then
        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertTrue(result.success());

        verify(consentRepositoryPort).findById(ConsentId.of(id));
        verify(consentRepositoryPort, never()).update(any());
    }

    @Test
    void compensateConsentWithdrawal_WhenConsentWithdrawn_ShouldRestoreAndCompensate() {
        // Given
        UUID id = UUID.randomUUID();

        Consent withdrawnConsent = testConsent.withStatus(ConsentStatus.WITHDRAWN);

        Consent restoredConsent = withdrawnConsent.withStatus(ConsentStatus.ACTIVE);

        when(consentRepositoryPort.findById(ConsentId.of(id)))
                .thenReturn(Optional.of(withdrawnConsent));

        when(consentRepositoryPort.update(any(Consent.class)))
                .thenReturn(restoredConsent);

        // When
        ResponseCompensated result = service.compensateConsentWithdrawalUpdate(id, String.class,
                SagaOutcome.COMPENSATE);

        // Then
        assertEquals(SagaOutcome.COMPENSATED, result.sagaState());
        assertTrue(result.success());

        verify(consentRepositoryPort).findById(ConsentId.of(id));
        verify(consentRepositoryPort).update(any(Consent.class));
    }

}