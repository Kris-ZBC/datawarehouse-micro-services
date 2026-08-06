package local.sop.datawarehouse.consent.saga.application.service.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.consent.saga.application.api.dto.ConsentStatementResponse;
import local.sop.datawarehouse.consent.saga.application.api.dto.CreateConsentStatementCmd;
import local.sop.datawarehouse.consent.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.datawarehouse.consent.saga.application.ports.out.consent.ConsentPort;
import local.sop.datawarehouse.consent.saga.application.ports.out.saga.ConsentSagaStatePort;
import local.sop.datawarehouse.consent.saga.application.service.ConsentSagaApplicationService;

/**
 * Integration test mimicking consent-handler behaviour:
 *
 * - Handler generates a sessionId before the first attempt
 * - Handler retries with the same sessionId on failure
 * - Handler fires two concurrent requests (duplicate delivery scenario)
 * - Handler uses different sessionIds for independent operations
 */
@ExtendWith(MockitoExtension.class)
class ConsentSagaHandlerIntegrationTest {

    @Mock private ConsentPort consents;
    @Mock private AuditlogPort auditlogs;
    @Mock private ConsentSagaStatePort sagaLock;

    private ConsentSagaApplicationService service;

    private UUID sessionId;
    private UUID consentStatementId;
    private UUID auditlogId;
    private ConsentStatementResponse statementResponse;
    private ResponseCompensated compensatedOk;

    @BeforeEach
    void setUp() {
        service = new ConsentSagaApplicationService(consents, auditlogs, sagaLock);

        sessionId          = UUID.randomUUID();
        consentStatementId = UUID.randomUUID();
        auditlogId         = UUID.randomUUID();

        statementResponse = new ConsentStatementResponse(
            consentStatementId, "Test consent statement text", true);

        compensatedOk = new ResponseCompensated(SagaOutcome.COMPENSATED, true);
    }

    // ── Scenario 1: Handler calls SAGA successfully ────────────────────────────

    @Test
    void handler_firstCall_shouldSucceed() {
        when(sagaLock.tryLock(any())).thenReturn(true);
        when(consents.create(any(), any())).thenReturn(consentStatementId);
        when(consents.getStatement(consentStatementId)).thenReturn(statementResponse);
        when(auditlogs.create(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(auditlogId);

        ConsentStatementResponse result = service.createConsentStatement(buildCmd(sessionId));

        assertNotNull(result);
        assertEquals(consentStatementId, result.consentStatementId());
        verify(sagaLock).release(sessionId);
    }

    // ── Scenario 2: Handler retries with same sessionId while lock is held ─────

    @Test
    void handler_retryWithSameSessionId_whileLockHeld_shouldGetAlreadyRunning() {
        // Lock is held (RUNNING) — retry gets rejected
        when(sagaLock.tryLock(any())).thenReturn(false);

        ConflictException ex = assertThrows(ConflictException.class,
            () -> service.createConsentStatement(buildCmd(sessionId)));

        assertEquals("saga.already.running", ex.getMessage());
        // SAGA never started downstream
        verify(consents, never()).create(any(), any());
    }

    // ── Scenario 3: Concurrent duplicate delivery — same sessionId ─────────────

    @Test
    void handler_concurrentDuplicateDelivery_onlyOneShouldProceed() throws Exception {
        // One thread acquires lock, the other gets rejected
        AtomicInteger lockAcquiredCount = new AtomicInteger(0);
        AtomicInteger lockRejectedCount = new AtomicInteger(0);

        when(sagaLock.tryLock(any())).thenAnswer(inv -> {
            // Simulate DB unique constraint — first call succeeds, second fails
            return lockAcquiredCount.getAndIncrement() == 0;
        });

        when(consents.create(any(), any())).thenReturn(consentStatementId);
        when(consents.getStatement(consentStatementId)).thenReturn(statementResponse);
        when(auditlogs.create(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(auditlogId);

        CountDownLatch latch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> {
            try {
                service.createConsentStatement(buildCmd(sessionId));
            } catch (ConflictException ex) {
                lockRejectedCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                service.createConsentStatement(buildCmd(sessionId));
            } catch (ConflictException ex) {
                lockRejectedCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executor.shutdown();

        // Exactly one succeeded, exactly one was rejected
        assertEquals(1, lockAcquiredCount.get() - lockRejectedCount.get());
        // Downstream called exactly once
        verify(consents, times(1)).create(any(), any());
    }

    // ── Scenario 4: Handler retries with new sessionId after compensation ──────

    @Test
    void handler_retryWithNewSessionId_afterCompensation_shouldSucceed() {
        // First attempt fails and compensates
        UUID firstSessionId = UUID.randomUUID();
        when(sagaLock.tryLock(argThat(l -> l != null && l.sessionId().equals(firstSessionId))))
            .thenReturn(true);
        when(consents.create(any(), any())).thenReturn(consentStatementId)
            .thenReturn(UUID.randomUUID()); // second attempt creates a new one
        when(consents.getStatement(consentStatementId))
            .thenThrow(new RuntimeException("timeout on first attempt"));
        when(consents.compensate(any(), any(), any())).thenReturn(compensatedOk);

        assertThrows(ConflictException.class,
            () -> service.createConsentStatement(buildCmd(firstSessionId)));
        verify(sagaLock).release(firstSessionId); // lock released after compensation

        // Second attempt with a fresh sessionId
        UUID secondSessionId = UUID.randomUUID();
        UUID secondConsentId = UUID.randomUUID();
        ConsentStatementResponse secondResponse =
            new ConsentStatementResponse(secondConsentId, "text", true);

        when(sagaLock.tryLock(argThat(l ->l != null && l.sessionId().equals(secondSessionId))))
            .thenReturn(true);
        when(consents.create(any(), any())).thenReturn(secondConsentId); // ← return secondConsentId
        when(consents.getStatement(secondConsentId)).thenReturn(secondResponse); // ← stub for secondConsentId
        when(auditlogs.create(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(auditlogId);


        ConsentStatementResponse result =
            service.createConsentStatement(buildCmd(secondSessionId));

        assertNotNull(result);
        verify(sagaLock).release(secondSessionId);
    }

    // ── Scenario 5: Independent operations from different handler sessions ──────

    @Test
    void handler_independentSessions_shouldNeverBlock_eachOther() {
        UUID sessionA = UUID.randomUUID();
        UUID sessionB = UUID.randomUUID();
        UUID consentA = UUID.randomUUID();
        UUID consentB = UUID.randomUUID();

        when(sagaLock.tryLock(argThat(l -> l != null && l.sessionId().equals(sessionA))))
            .thenReturn(true);
        when(sagaLock.tryLock(argThat(l -> l != null && l.sessionId().equals(sessionB))))
            .thenReturn(true);

        when(consents.create(any(), any()))
            .thenReturn(consentA)
            .thenReturn(consentB);
        when(consents.getStatement(consentA))
            .thenReturn(new ConsentStatementResponse(consentA, "text A", true));
        when(consents.getStatement(consentB))
            .thenReturn(new ConsentStatementResponse(consentB, "text B", true));
        when(auditlogs.create(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(UUID.randomUUID());

        // Both sessions succeed independently
        ConsentStatementResponse resultA =
            service.createConsentStatement(buildCmd(sessionA));
        ConsentStatementResponse resultB =
            service.createConsentStatement(buildCmd(sessionB));

        assertNotNull(resultA);
        assertNotNull(resultB);
        assertNotEquals(resultA.consentStatementId(), resultB.consentStatementId());
        verify(sagaLock).release(sessionA);
        verify(sagaLock).release(sessionB);
    }

    // ── Scenario 6: Full compensation chain — handler observes failure ─────────

    @Test
    void handler_observesConflictException_whenCompensationCompletes() {
        when(sagaLock.tryLock(any())).thenReturn(true);
        when(consents.create(any(), any())).thenReturn(consentStatementId);
        when(consents.getStatement(consentStatementId)).thenReturn(statementResponse);
        when(auditlogs.create(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("auditlog BC unreachable"));
        when(consents.compensate(any(), any(), any())).thenReturn(compensatedOk);

        // Handler receives ConflictException — knows to log and alert
        ConflictException ex = assertThrows(ConflictException.class,
            () -> service.createConsentStatement(buildCmd(sessionId)));

        assertEquals("auditlog.notcreated", ex.getMessage());

        // Consent statement was compensated — no orphaned data
        verify(consents).compensate(
            eq(consentStatementId),
            eq(ConsentSagaApplicationService.class),
            eq(SagaOutcome.COMPENSATED));

        // Lock was released — handler can decide to retry with new sessionId
        verify(sagaLock).release(sessionId);
    }

    // ── Builder helper ─────────────────────────────────────────────────────────

    private CreateConsentStatementCmd buildCmd(UUID sessionId) {
        return new CreateConsentStatementCmd(
            sessionId, true, "Test consent statement text",
            UUID.randomUUID(), ActorType.USER, Severity.INFO,
            "originSystem", "originService", "originComponent",
            "data", "description"
        );
    }
}
