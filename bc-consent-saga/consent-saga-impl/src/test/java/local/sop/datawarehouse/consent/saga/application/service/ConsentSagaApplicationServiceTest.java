package local.sop.datawarehouse.consent.saga.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.enums.ConsentType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.DomainException;
import local.sop.common.libs.sharedkernel.exceptions.ErrorCode;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.common.libs.sharedkernel.sagas.concurrency.locks.SagaStatus;
import local.sop.datawarehouse.consent.saga.application.api.dto.ConsentResponse;
import local.sop.datawarehouse.consent.saga.application.api.dto.ConsentStatementResponse;
import local.sop.datawarehouse.consent.saga.application.api.dto.CreateConsentStatementCmd;
import local.sop.datawarehouse.consent.saga.application.api.dto.GrantConsentCmd;
import local.sop.datawarehouse.consent.saga.application.api.dto.RevokeConsentCmd;
import local.sop.datawarehouse.consent.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.datawarehouse.consent.saga.application.ports.out.consent.ConsentPort;
import local.sop.datawarehouse.consent.saga.application.ports.out.saga.ConsentSagaStatePort;

@ExtendWith(MockitoExtension.class)
class ConsentSagaApplicationServiceTest {

    @Mock private ConsentPort consents;
    @Mock private AuditlogPort auditlogs;
    @Mock private ConsentSagaStatePort sagaLock;

    private ConsentSagaApplicationService service;

    private UUID sessionId;
    private UUID consentStatementId;
    private UUID auditlogId;
    private UUID personId;
    private UUID consentId;

    private CreateConsentStatementCmd createCmd;
    private ConsentStatementResponse statementResponse;
    private ConsentResponse consentResponse;
    private ResponseCompensated compensatedOk;
    private ResponseCompensated compensatedFailed;

    @BeforeEach
    void setUp() {
        service = new ConsentSagaApplicationService(consents, auditlogs, sagaLock);

        sessionId           = UUID.randomUUID();
        consentStatementId  = UUID.randomUUID();
        auditlogId          = UUID.randomUUID();
        personId            = UUID.randomUUID();
        consentId           = UUID.randomUUID();

        createCmd = new CreateConsentStatementCmd(
            sessionId, true, "Test consent statement text",
            ConsentPurpose.MARKETING, ConsentType.OPTIONAL,
            UUID.randomUUID(), ActorType.USER, Severity.INFO,
            "originSystem", "originService", "originComponent",
            "data", "description"
        );

        statementResponse = new ConsentStatementResponse(
            consentStatementId, createCmd.statementText(), createCmd.active(), createCmd.purpose().name(), createCmd.type().name());

        consentResponse = new ConsentResponse(
            consentId, personId, ConsentStatus.ACTIVE.name(),
            consentStatementId, createCmd.statementText(),
            ConsentPurpose.COMMUNICATION, ConsentType.REQUIRED,
            createCmd.active());

        compensatedOk     = new ResponseCompensated(SagaOutcome.COMPENSATED, true);
        compensatedFailed = new ResponseCompensated(SagaOutcome.COMPENSATED, false);
    }

    // ── Session lock ───────────────────────────────────────────────────────────

    @Nested
    class SessionLock {

        @Test
        void createConsentStatement_shouldThrowConflictException_whenLockAlreadyExists() {
            when(sagaLock.tryLock(any())).thenReturn(false);

            ConflictException ex = assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));

            assertEquals("saga.already.running", ex.getMessage());
        }

        @Test
        void createConsentStatement_shouldNeverCallConsents_whenLockAlreadyExists() {
            when(sagaLock.tryLock(any())).thenReturn(false);

            assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));

            verify(consents, never()).create(any(), any(), any(), any());
        }

        @Test
        void createConsentStatement_shouldAcquireLockWithSessionId() {
            when(sagaLock.tryLock(any())).thenReturn(false);

            assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));

            verify(sagaLock).tryLock(argThat(lock -> lock.sessionId().equals(sessionId)));
        }

        @Test
        void createConsentStatement_shouldReleaseLock_whenAllStepsSucceed() {
            when(sagaLock.tryLock(any())).thenReturn(true);
            when(consents.create(any(), any(), any(), any())).thenReturn(consentStatementId);
            when(consents.getStatement(consentStatementId)).thenReturn(statementResponse);
            when(auditlogs.create(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(auditlogId);

            service.createConsentStatement(createCmd);

            verify(sagaLock).release(sessionId);
        }

        @Test
        void createConsentStatement_shouldReleaseLock_whenConsentCreateFails() {
            when(sagaLock.tryLock(any())).thenReturn(true);
            when(consents.create(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("downstream unavailable"));

            assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));

            verify(sagaLock).release(sessionId);
        }

        @Test
        void createConsentStatement_shouldReleaseLock_afterCompensation() {
            when(sagaLock.tryLock(any())).thenReturn(true);
            when(consents.create(any(), any(), any(), any())).thenReturn(consentStatementId);
            when(consents.getStatement(consentStatementId)).thenReturn(null);
            when(consents.compensate(any(), any(), any())).thenReturn(compensatedOk);

            assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));

            verify(sagaLock).release(sessionId);
        }

        @Test
        void createConsentStatement_shouldReleaseLock_evenWhenCompensationFails() {
            when(sagaLock.tryLock(any())).thenReturn(true);
            when(consents.create(any(), any(), any(), any())).thenReturn(consentStatementId);
            when(consents.getStatement(consentStatementId)).thenReturn(null);
            when(consents.compensate(any(), any(), any()))
                .thenThrow(new RuntimeException("compensation also failed"));

            // lock must always be released — even if compensation throws
            assertThrows(RuntimeException.class,
                () -> service.createConsentStatement(createCmd));

            verify(sagaLock).release(sessionId);
        }
    }

    // ── Happy path ─────────────────────────────────────────────────────────────

    @Nested
    class HappyPath {

        @BeforeEach
        void lockAcquired() {
            when(sagaLock.tryLock(any())).thenReturn(true);
        }

        @Test
        void createConsentStatement_shouldReturnStatementResponse_whenAllStepsSucceed() {
            when(consents.create(createCmd.active(), createCmd.statementText(), createCmd.purpose(), createCmd.type()))
                .thenReturn(consentStatementId);
            when(consents.getStatement(consentStatementId)).thenReturn(statementResponse);
            when(auditlogs.create(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(auditlogId);

            ConsentStatementResponse result = service.createConsentStatement(createCmd);

            assertNotNull(result);
            assertEquals(consentStatementId, result.consentStatementId());
            assertEquals(createCmd.statementText(), result.statementText());
            assertEquals(createCmd.active(), result.active());
        }

        @Test
        void createConsentStatement_shouldInvokeAllStepsInOrder() {
            when(consents.create(any(), any(), any(), any())).thenReturn(consentStatementId);
            when(consents.getStatement(consentStatementId)).thenReturn(statementResponse);
            when(auditlogs.create(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(auditlogId);

            service.createConsentStatement(createCmd);

            var inOrder = inOrder(consents, auditlogs);
            inOrder.verify(consents).create(createCmd.active(), createCmd.statementText(), createCmd.purpose(), createCmd.type());
            inOrder.verify(consents).getStatement(consentStatementId);
            inOrder.verify(auditlogs).create(any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void createConsentStatement_shouldNeverCallCompensate_whenAllStepsSucceed() {
            when(consents.create(any(), any(), any(), any())).thenReturn(consentStatementId);
            when(consents.getStatement(consentStatementId)).thenReturn(statementResponse);
            when(auditlogs.create(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(auditlogId);

            service.createConsentStatement(createCmd);

            verify(consents, never()).compensate(any(), any(), any());
            verify(auditlogs, never()).compensate(any(), any(), any());
        }

        @Test
        void grant_shouldReturnConsentResponse_whenAllStepsSucceed() {
            GrantConsentCmd cmd = buildGrantCmd();

            when(consents.getStatement(consentStatementId)).thenReturn(statementResponse);
            when(consents.grant(any(), any(), any())).thenReturn(consentResponse);
            when(consents.getConsent(consentId)).thenReturn(consentResponse);
            when(auditlogs.create(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(auditlogId);

            ConsentResponse result = service.grant(cmd);

            assertNotNull(result);
            assertEquals(consentStatementId, result.consentStatementId());
            assertEquals(consentId, result.consentId());
        }

        @Test
        void grant_shouldNeverCallCompensate_whenAllStepsSucceed() {
            GrantConsentCmd cmd = buildGrantCmd();

            when(consents.getStatement(consentStatementId)).thenReturn(statementResponse);
            when(consents.grant(any(), any(), any())).thenReturn(consentResponse);
            when(consents.getConsent(consentId)).thenReturn(consentResponse);
            when(auditlogs.create(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(auditlogId);

            service.grant(cmd);

            verify(consents, never()).compensateConsent(any(), any(), any());
        }

        @Test
        void withdraw_shouldReturnConsentResponse_whenAllStepsSucceed() {
            RevokeConsentCmd cmd = buildRevokeCmd();

            when(consents.getConsent(consentId)).thenReturn(consentResponse);
            when(consents.withdraw(consentId)).thenReturn(consentResponse);
            when(auditlogs.create(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(auditlogId);

            ConsentResponse result = service.withdraw(cmd);

            assertNotNull(result);
            assertEquals(consentId, result.consentId());
        }

        @Test
        void withdraw_shouldNeverCallCompensate_whenAllStepsSucceed() {
            RevokeConsentCmd cmd = buildRevokeCmd();

            when(consents.getConsent(consentId)).thenReturn(consentResponse);
            when(consents.withdraw(consentId)).thenReturn(consentResponse);
            when(auditlogs.create(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(auditlogId);

            service.withdraw(cmd);

            verify(consents, never()).compensateConsentUpdate(any(), any(), any());
        }
    }

    // ── createConsentStatement — Step 1: consent create failures ──────────────

    @Nested
    class CreateConsentStatement_Step1_ConsentCreate {

        @BeforeEach
        void lockAcquired() {
            when(sagaLock.tryLock(any())).thenReturn(true);
        }

        @Test
        void shouldThrowConflictException_whenConsentCreateThrowsRuntimeException() {
            when(consents.create(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("downstream unavailable"));

            assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));
        }

        @Test
        void shouldNotProceedToGetOrAuditlog_whenConsentCreateFails() {
            when(consents.create(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("downstream unavailable"));

            assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));

            verify(consents, never()).getStatement(any());
            verify(auditlogs, never()).create(any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void shouldNotCompensate_whenConsentCreateFails() {
            when(consents.create(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("downstream unavailable"));

            assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));

            verify(consents, never()).compensate(any(), any(), any());
        }

        @Test
        void shouldRethrowDomainException_whenConsentCreateThrowsDomainException() {
            var domainEx = new DomainException(ErrorCode.CONFLICT, "some.domain.error",
                Map.of("id", consentStatementId.toString()));
            when(consents.create(any(), any(), any(), any())).thenThrow(domainEx);

            DomainException thrown = assertThrows(DomainException.class,
                () -> service.createConsentStatement(createCmd));
            assertSame(domainEx, thrown);
        }
    }

    // ── createConsentStatement — Step 2: sanity check failures ────────────────

    @Nested
    class CreateConsentStatement_Step2_SanityCheck {

        @BeforeEach
        void lockAcquiredAndStep1Succeeds() {
            when(sagaLock.tryLock(any())).thenReturn(true);
            when(consents.create(any(), any(), any(), any())).thenReturn(consentStatementId);
        }

        @Test
        void shouldThrowConflictException_whenGetReturnsNull() {
            when(consents.getStatement(consentStatementId)).thenReturn(null);
            when(consents.compensate(any(), any(), any())).thenReturn(compensatedOk);

            assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));
        }

        @Test
        void shouldCompensateConsent_whenGetReturnsNull() {
            when(consents.getStatement(consentStatementId)).thenReturn(null);
            when(consents.compensate(any(), any(), any())).thenReturn(compensatedOk);

            assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));

            verify(consents).compensate(
                eq(consentStatementId),
                eq(ConsentSagaApplicationService.class),
                eq(SagaOutcome.COMPENSATED));
        }

        @Test
        void shouldNotCallAuditlog_whenGetReturnsNull() {
            when(consents.getStatement(consentStatementId)).thenReturn(null);
            when(consents.compensate(any(), any(), any())).thenReturn(compensatedOk);

            assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));

            verify(auditlogs, never()).create(any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void shouldThrowConflictException_whenGetThrowsRuntimeException() {
            when(consents.getStatement(consentStatementId))
                .thenThrow(new RuntimeException("timeout"));
            when(consents.compensate(any(), any(), any())).thenReturn(compensatedOk);

            assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));
        }

        @Test
        void shouldCompensateConsent_whenGetThrowsRuntimeException() {
            when(consents.getStatement(consentStatementId))
                .thenThrow(new RuntimeException("timeout"));
            when(consents.compensate(any(), any(), any())).thenReturn(compensatedOk);

            assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));

            verify(consents).compensate(eq(consentStatementId), any(), eq(SagaOutcome.COMPENSATED));
        }

        @Test
        void shouldRethrowDomainException_whenGetThrowsDomainException() {
            var domainEx = new DomainException(ErrorCode.CONFLICT, "some.domain.error",
                Map.of("id", consentStatementId.toString()));
            when(consents.getStatement(consentStatementId)).thenThrow(domainEx);

            DomainException thrown = assertThrows(DomainException.class,
                () -> service.createConsentStatement(createCmd));
            assertSame(domainEx, thrown);
        }

        @Test
        void shouldUpdateLockToCompensating_whenGetReturnsNull() {
            when(consents.getStatement(consentStatementId)).thenReturn(null);
            when(consents.compensate(any(), any(), any())).thenReturn(compensatedOk);

            assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
        }
    }

    // ── createConsentStatement — Step 3: auditlog failures ────────────────────

    @Nested
    class CreateConsentStatement_Step3_AuditlogCreate {

        @BeforeEach
        void lockAcquiredAndSteps1And2Succeed() {
            when(sagaLock.tryLock(any())).thenReturn(true);
            when(consents.create(any(), any(), any(), any())).thenReturn(consentStatementId);
            when(consents.getStatement(consentStatementId)).thenReturn(statementResponse);
        }

        @Test
        void shouldThrowConflictException_whenAuditlogCreateThrowsRuntimeException() {
            when(auditlogs.create(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("auditlog service down"));
            when(consents.compensate(any(), any(), any())).thenReturn(compensatedOk);

            assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));
        }

        @Test
        void shouldCompensateConsent_whenAuditlogCreateFails() {
            when(auditlogs.create(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("auditlog service down"));
            when(consents.compensate(any(), any(), any())).thenReturn(compensatedOk);

            assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));

            verify(consents).compensate(
                eq(consentStatementId),
                eq(ConsentSagaApplicationService.class),
                eq(SagaOutcome.COMPENSATED));
        }

        @Test
        void shouldRethrowDomainException_whenAuditlogCreateThrowsDomainException() {
            var domainEx = new DomainException(ErrorCode.CONFLICT, "some.domain.error",
                Map.of("id", consentStatementId.toString()));
            when(auditlogs.create(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(domainEx);
            when(consents.compensate(any(), any(), any())).thenReturn(compensatedOk);

            DomainException thrown = assertThrows(DomainException.class,
                () -> service.createConsentStatement(createCmd));
            assertSame(domainEx, thrown);
        }

        @Test
        void shouldUpdateLockToCompensating_whenAuditlogFails() {
            when(auditlogs.create(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("auditlog down"));
            when(consents.compensate(any(), any(), any())).thenReturn(compensatedOk);

            assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
        }
    }

    // ── grant — failures ───────────────────────────────────────────────────────

    @Nested
    class Grant_Failures {

        @BeforeEach
        void lockAcquired() {
            when(sagaLock.tryLock(any())).thenReturn(true);
        }

        @Test
        void shouldThrowConflictException_whenLockAlreadyExists() {
            reset(sagaLock);
            when(sagaLock.tryLock(any())).thenReturn(false);

            assertThrows(ConflictException.class,
                () -> service.grant(buildGrantCmd()));
        }

        @Test
        void shouldThrowConflictException_whenConsentStatementNotFound() {
            when(consents.getStatement(consentStatementId)).thenReturn(null);

            assertThrows(ConflictException.class,
                () -> service.grant(buildGrantCmd()));

            verify(consents, never()).grant(any(), any(), any());
        }

        @Test
        void shouldReleaseLock_whenConsentStatementNotFound() {
            when(consents.getStatement(consentStatementId)).thenReturn(null);

            assertThrows(ConflictException.class,
                () -> service.grant(buildGrantCmd()));

            verify(sagaLock, atLeastOnce()).release(sessionId);
        }

        @Test
        void shouldThrowConflictException_whenGrantFails() {
            when(consents.getStatement(consentStatementId)).thenReturn(statementResponse);
            when(consents.grant(any(), any(), any()))
                .thenThrow(new RuntimeException("grant failed"));

            assertThrows(ConflictException.class,
                () -> service.grant(buildGrantCmd()));
        }

        @Test
        void shouldCompensateConsent_whenSanityCheckAfterGrantReturnsNull() {
            when(consents.getStatement(consentStatementId)).thenReturn(statementResponse);
            when(consents.grant(any(), any(), any())).thenReturn(consentResponse);
            when(consents.getConsent(consentId)).thenReturn(null);
            when(consents.compensateConsentUpdate(any(), any(), any())).thenReturn(compensatedOk);

            assertThrows(ConflictException.class,
                () -> service.grant(buildGrantCmd()));

            verify(consents).compensateConsentUpdate(
                eq(consentId),
                eq(ConsentSagaApplicationService.class),
                eq(SagaOutcome.COMPENSATED));
        }

        @Test
        void shouldCompensateConsent_whenAuditlogFailsAfterGrant() {
            when(consents.getStatement(consentStatementId)).thenReturn(statementResponse);
            when(consents.grant(any(), any(), any())).thenReturn(consentResponse);
            when(consents.getConsent(consentId)).thenReturn(consentResponse);
            when(auditlogs.create(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("auditlog down"));
            when(consents.compensateConsent(any(), any(), any())).thenReturn(compensatedOk);

            assertThrows(ConflictException.class,
                () -> service.grant(buildGrantCmd()));

            verify(consents).compensateConsent(
                eq(consentId),
                eq(ConsentSagaApplicationService.class),
                eq(SagaOutcome.COMPENSATED));
        }

        @Test
        void shouldThrowConflictException_whenGetConsentStatementThrowsRuntimeException() {
            when(consents.getStatement(consentStatementId))
                    .thenThrow(new RuntimeException("service unavailable"));

            ConflictException ex = assertThrows(
                    ConflictException.class,
                    () -> service.grant(buildGrantCmd()));

            assertEquals("consentstatement.read.failed", ex.getMessage());
        }

        @Test
        void shouldReleaseLock_whenGetConsentStatementThrowsRuntimeException() {
            when(consents.getStatement(consentStatementId))
                    .thenThrow(new RuntimeException("service unavailable"));

            assertThrows(
                    ConflictException.class,
                    () -> service.grant(buildGrantCmd()));

            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldRethrowDomainException_whenGetConsentStatementThrowsDomainException() {
            DomainException domainEx = new DomainException(
                    ErrorCode.CONFLICT,
                    "consentstatement.read.failed",
                    Map.of("id", consentStatementId.toString()));

            when(consents.getStatement(consentStatementId))
                    .thenThrow(domainEx);

            DomainException thrown = assertThrows(
                    DomainException.class,
                    () -> service.grant(buildGrantCmd()));

            assertSame(domainEx, thrown);
        }

        @Test
        void shouldReleaseLock_whenGetConsentStatementThrowsDomainException() {
            DomainException domainEx = new DomainException(
                    ErrorCode.CONFLICT,
                    "consentstatement.read.failed",
                    Map.of("id", consentStatementId.toString()));

            when(consents.getStatement(consentStatementId))
                    .thenThrow(domainEx);

            assertThrows(
                    DomainException.class,
                    () -> service.grant(buildGrantCmd()));

            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldSetSagaStatusToCompensating_whenConsentGetFailsAfterGrant() {
            when(consents.getStatement(consentStatementId)).thenReturn(statementResponse);
            when(consents.grant(any(), any(), any())).thenReturn(consentResponse);

            when(consents.getConsent(consentId))
                    .thenThrow(new RuntimeException("db down"));

            assertThrows(
                    ConflictException.class,
                    () -> service.grant(buildGrantCmd()));

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);
        }

        @Test
        void shouldCallCompensateConsent_whenConsentGetFailsAfterGrant() {
            when(consents.getStatement(consentStatementId)).thenReturn(statementResponse);
            when(consents.grant(any(), any(), any())).thenReturn(consentResponse);
            when(consents.compensateConsentUpdate(any(), any(), any())).thenReturn(compensatedOk);

            when(consents.getConsent(consentId))
                    .thenThrow(new RuntimeException("db down"));

            assertThrows(
                    ConflictException.class,
                    () -> service.grant(buildGrantCmd()));

            verify(consents).compensateConsentUpdate(
                    eq(consentId),
                    eq(ConsentSagaApplicationService.class),
                    eq(SagaOutcome.COMPENSATED));
        }


        @Test
        void shouldRethrowDomainException_whenConsentGetFailsAfterGrant() {
            when(consents.getStatement(consentStatementId)).thenReturn(statementResponse);
            when(consents.grant(any(), any(), any())).thenReturn(consentResponse);
            when(consents.compensateConsentUpdate(any(), any(), any())).thenReturn(compensatedOk);
            DomainException ex = new DomainException(
                    ErrorCode.CONFLICT,
                    "consent.read.failed",
                    Map.of("id", consentId.toString()));

            when(consents.getConsent(consentId)).thenThrow(ex);

            DomainException thrown = assertThrows(
                    DomainException.class,
                    () -> service.grant(buildGrantCmd()));

            assertSame(ex, thrown);

            verify(consents).compensateConsentUpdate(
                    eq(consentId),
                    eq(ConsentSagaApplicationService.class),
                    eq(SagaOutcome.COMPENSATED));

            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldReleaseLockAndThrowConflictException_whenConsentGetThrowsRuntimeException() {
            // given
            when(consents.getStatement(consentStatementId)).thenReturn(statementResponse);
            when(consents.grant(any(), any(), any())).thenReturn(consentResponse);

            when(consents.getConsent(consentId))
                    .thenThrow(new RuntimeException("db down"));

            // when
            ConflictException ex = assertThrows(
                    ConflictException.class,
                    () -> service.grant(buildGrantCmd()));

            // then
            assertEquals("consent.read.failed", ex.getMessage());

            verify(sagaLock).release(sessionId);

            verify(consents).getConsent(consentId);
        }
       
        @Test
        void shouldUpdateStatusAndCompensateUpdate_whenConsentGetThrowsRuntimeException() {
            // given
            when(consents.getStatement(consentStatementId)).thenReturn(statementResponse);
            when(consents.grant(any(), any(), any())).thenReturn(consentResponse);
            when(consents.compensateConsentUpdate(any(), any(), any())).thenReturn(compensatedOk);

            when(consents.getConsent(consentId))
                    .thenThrow(new RuntimeException("db down"));

            // when
            ConflictException ex = assertThrows(
                    ConflictException.class,
                    () -> service.grant(buildGrantCmd()));

            // then
            assertEquals("consent.read.failed", ex.getMessage());

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);

            verify(consents).compensateConsentUpdate(
                    eq(consentId),
                    eq(ConsentSagaApplicationService.class),
                    eq(SagaOutcome.COMPENSATED));
        }


        @Test
        void shouldUpdateStatusAndCompensateUpdate_whenConsentGetThrowsDomainException() {
            // given
            when(consents.getStatement(consentStatementId)).thenReturn(statementResponse);
            when(consents.grant(any(), any(), any())).thenReturn(consentResponse);
            when(consents.compensateConsentUpdate(any(), any(), any())).thenReturn(compensatedOk);
            DomainException ex = new DomainException(
                    ErrorCode.CONFLICT,
                    "consent.read.failed",
                    Map.of("id", consentId.toString()));

            when(consents.getConsent(consentId)).thenThrow(ex);

            // when
            DomainException thrown = assertThrows(
                    DomainException.class,
                    () -> service.grant(buildGrantCmd()));

            // then
            assertSame(ex, thrown);

            verify(sagaLock).updateStatus(sessionId, SagaStatus.COMPENSATING);

            verify(consents).compensateConsentUpdate(
                    eq(consentId),
                    eq(ConsentSagaApplicationService.class),
                    eq(SagaOutcome.COMPENSATED));
        }
        
    }

    // ── withdraw — failures ────────────────────────────────────────────────────

    @Nested
    class Withdraw_Failures {

        @BeforeEach
        void lockAcquired() {
            when(sagaLock.tryLock(any())).thenReturn(true);
        }

        @Test
        void shouldThrowConflictException_whenLockAlreadyExists() {
            reset(sagaLock);
            when(sagaLock.tryLock(any())).thenReturn(false);

            assertThrows(ConflictException.class,
                () -> service.withdraw(buildRevokeCmd()));
        }

        @Test
        void shouldThrowConflictException_whenConsentNotFound() {
            when(consents.getConsent(consentId)).thenReturn(null);

            assertThrows(ConflictException.class,
                () -> service.withdraw(buildRevokeCmd()));

            verify(consents, never()).withdraw(any());
        }

        @Test
        void shouldReleaseLock_whenConsentNotFound() {
            when(consents.getConsent(consentId)).thenReturn(null);

            assertThrows(ConflictException.class,
                () -> service.withdraw(buildRevokeCmd()));

            verify(sagaLock, atLeastOnce()).release(sessionId);
        }

        @Test
        void shouldThrowConflictException_whenWithdrawFails() {
            when(consents.getConsent(consentId)).thenReturn(consentResponse);
            when(consents.withdraw(consentId))
                .thenThrow(new RuntimeException("withdraw failed"));

            assertThrows(ConflictException.class,
                () -> service.withdraw(buildRevokeCmd()));
        }

        @Test
        void shouldCompensateConsentUpdate_whenSanityCheckAfterWithdrawReturnsNull() {
            when(consents.getConsent(consentId))
                .thenReturn(consentResponse)  // pre-check
                .thenReturn(null);            // post-check
            when(consents.withdraw(consentId)).thenReturn(consentResponse);
            when(consents.compensateConsentUpdate(any(), any(), any())).thenReturn(compensatedOk);

            assertThrows(ConflictException.class,
                () -> service.withdraw(buildRevokeCmd()));

            verify(consents).compensateConsentUpdate(
                eq(consentId),
                eq(ConsentSagaApplicationService.class),
                eq(SagaOutcome.COMPENSATED));
        }

        @Test
        void shouldCompensateConsentUpdate_whenAuditlogFailsAfterWithdraw() {
            when(consents.getConsent(consentId)).thenReturn(consentResponse);
            when(consents.withdraw(consentId)).thenReturn(consentResponse);
            when(auditlogs.create(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("auditlog down"));
            when(consents.compensateConsentUpdate(any(), any(), any())).thenReturn(compensatedOk);

            assertThrows(ConflictException.class,
                () -> service.withdraw(buildRevokeCmd()));

            verify(consents).compensateConsentUpdate(
                eq(consentId),
                eq(ConsentSagaApplicationService.class),
                eq(SagaOutcome.COMPENSATED));
        }
    }

    // ── Compensation edge cases ────────────────────────────────────────────────

    @Nested
    class CompensationEdgeCases {

        @BeforeEach
        void lockAcquired() {
            when(sagaLock.tryLock(any())).thenReturn(true);
        }

        @Test
        void shouldStillThrowConflictException_evenWhenCompensationFails() {
            when(consents.create(any(), any(), any(), any())).thenReturn(consentStatementId);
            when(consents.getStatement(consentStatementId)).thenReturn(null);
            when(consents.compensate(any(), any(), any())).thenReturn(compensatedFailed);

            assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));
        }

        @Test
        void shouldStillReleaseLock_evenWhenCompensationReturnsFailure() {
            when(consents.create(any(), any(), any(), any())).thenReturn(consentStatementId);
            when(consents.getStatement(consentStatementId)).thenReturn(null);
            when(consents.compensate(any(), any(), any())).thenReturn(compensatedFailed);

            assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));

            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldStillReleaseLock_evenWhenCompensationThrows() {
            when(consents.create(any(), any(), any(), any())).thenReturn(consentStatementId);
            when(consents.getStatement(consentStatementId)).thenReturn(null);
            when(consents.compensate(any(), any(), any()))
                .thenThrow(new RuntimeException("compensation also failed"));

            assertThrows(RuntimeException.class,
                () -> service.createConsentStatement(createCmd));

            verify(sagaLock).release(sessionId);
        }

        @Test
        void shouldCompensateWithCorrectSagaOutcome() {
            when(consents.create(any(), any(), any(), any())).thenReturn(consentStatementId);
            when(consents.getStatement(consentStatementId)).thenReturn(null);
            when(consents.compensate(any(), any(), any())).thenReturn(compensatedOk);

            assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));

            verify(consents).compensate(any(), any(), eq(SagaOutcome.COMPENSATED));
        }

        @Test
        void shouldPassServiceClassToCompensate() {
            when(consents.create(any(), any(), any(), any())).thenReturn(consentStatementId);
            when(consents.getStatement(consentStatementId)).thenReturn(null);
            when(consents.compensate(any(), any(), any())).thenReturn(compensatedOk);

            assertThrows(ConflictException.class,
                () -> service.createConsentStatement(createCmd));

            verify(consents).compensate(
                eq(consentStatementId),
                eq(ConsentSagaApplicationService.class),
                eq(SagaOutcome.COMPENSATED));
        }
    }

    



    // ── Builder helpers ────────────────────────────────────────────────────────

    private GrantConsentCmd buildGrantCmd() {
        return new GrantConsentCmd(
            sessionId, personId, consentStatementId,
            ConsentStatus.ACTIVE,
            personId, ActorType.USER, Severity.INFO,
            "originSystem", "originService", "originComponent",
            "data", "description"
        );
    }

    private RevokeConsentCmd buildRevokeCmd() {
        return new RevokeConsentCmd(
            sessionId, consentId,
            personId, ActorType.USER, Severity.INFO,
            "originSystem", "originService", "originComponent",
            "data", "description"
        );
    }
}