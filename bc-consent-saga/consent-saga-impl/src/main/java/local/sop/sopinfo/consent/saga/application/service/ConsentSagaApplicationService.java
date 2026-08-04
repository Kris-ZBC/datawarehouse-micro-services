package local.sop.sopinfo.consent.saga.application.service;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import local.sop.sopinfo.consent.saga.application.api.ConsentSagaDirectory;
import local.sop.sopinfo.consent.saga.application.api.dto.ConsentResponse;
import local.sop.sopinfo.consent.saga.application.api.dto.ConsentStatementResponse;
import local.sop.sopinfo.consent.saga.application.api.dto.CreateConsentStatementCmd;
import local.sop.sopinfo.consent.saga.application.api.dto.GrantConsentCmd;
import local.sop.sopinfo.consent.saga.application.api.dto.RevokeConsentCmd;
import local.sop.sopinfo.consent.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.sopinfo.consent.saga.application.ports.out.consent.ConsentPort;
import local.sop.sopinfo.consent.saga.application.ports.out.saga.ConsentSagaStatePort;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.DomainException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.concurrency.locks.SagaStatus;
import local.sop.common.libs.sharedkernel.sagas.concurrency.locks.SagaConcurrencyLock;

@Service
public class ConsentSagaApplicationService implements ConsentSagaDirectory {

     private static final Logger log = LoggerFactory.getLogger(ConsentSagaApplicationService.class);

    private final ConsentPort consents;
    private final AuditlogPort auditlogs;
    private final ConsentSagaStatePort sagaLock;


    public ConsentSagaApplicationService(ConsentPort consents, AuditlogPort auditlogs, ConsentSagaStatePort sagaLock) {
        this.consents = consents;
        this.auditlogs = auditlogs;
        this.sagaLock = sagaLock;
    }



 /*
     * SAGA: create consent statement + auditlog
     *
     * Steps:
     *   1. Acquire session lock (concurrency guard)
     *   2. Create consent statement
     *   3. Sanity-check consent statement is readable
     *   4. Create auditlog
     *   5. Release lock
     *
     * The lock row exists only while the SAGA is in flight.
     * On success or full compensation the row is deleted.
     * A stale lock (crashed SAGA) is detectable via locked_at timestamp.
     *
     * Retry safety:
     *   The caller (consent-handler) supplies sessionId and retries with
     *   the same sessionId. While the lock exists, retries get saga.already.running.
     *   After the lock is released (success or compensation), the caller
     *   can re-query the downstream BC directly for the result.
     *
     * Concurrency:
     *   tryLock() exploits the DB primary key uniqueness constraint.
     *   Two concurrent requests with the same sessionId — one acquires
     *   the lock, the other gets ConflictException.
     *   Two requests with different sessionIds — no blocking.
     */
   @Override
   @Transactional
   public ConsentStatementResponse createConsentStatement(CreateConsentStatementCmd cmd) {

        UUID sessionId = cmd.sessionId();

        /* ── Acquire session lock ───────────────────────────────────────────── */
        if (!sagaLock.tryLock(SagaConcurrencyLock.start(sessionId))) {
            throw new ConflictException("saga.already.running",
                Map.of("sessionId", sessionId.toString()));
        }

        log.info("SAGA [{}]: lock acquired, starting createConsentStatement", sessionId);

        /* ── Step 1: Create consent statement ──────────────────────────────── */
        UUID consentStatementId;
        try {
            consentStatementId = consents.create(cmd.active(), cmd.statementText());
            log.info("SAGA [{}]: consent statement created {}",
                sessionId, consentStatementId);
        } catch (DomainException ex) {
            sagaLock.release(sessionId);
            throw ex;
        } catch (RuntimeException ex) {
            sagaLock.release(sessionId);
            log.warn("SAGA [{}]: consent statement create failed", sessionId);
            throw new ConflictException("consentstatement.notcreated",
                Map.of("object", "consentStatement"));
        }

        /* ── Step 2: Sanity check ──────────────────────────────────────────── */
        ConsentStatementResponse response;
        try {
            response = consents.getStatement(consentStatementId);
            if (response == null) {
                sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
                compensateConsentStatement(sessionId, consentStatementId);
                throw new ConflictException("consentstatement.notcreated",
                    Map.of("object", "consentStatement"));
            }
        } catch (DomainException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
            log.warn("SAGA [{}]: consent get failed for {}, compensating",
                sessionId, consentStatementId);
            compensateConsentStatement(sessionId, consentStatementId);
            throw new ConflictException("consentstatement.read_failed",
                Map.of("id", consentStatementId.toString()));
        }

        /* ── Step 3: Create auditlog ───────────────────────────────────────── */
        try {
            UUID auditlogId = auditlogs.create(
                cmd.actorRef(), cmd.actorType(), cmd.severity(),
                cmd.originSystem(), cmd.originService(), cmd.originComponent(),
                cmd.data(), cmd.description());
            log.info("SAGA [{}]: auditlog created {}", sessionId, auditlogId);
        } catch (DomainException ex) {
            sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
            compensateConsentStatement(sessionId, consentStatementId);
            throw ex;
        } catch (RuntimeException ex) {
            sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
            log.warn("SAGA [{}]: auditlog create failed, compensating consent {}",
                sessionId, consentStatementId);
            compensateConsentStatement(sessionId, consentStatementId);
            throw new ConflictException("auditlog.notcreated",
                Map.of("object", "auditlog"));
        }

        /* ── All steps succeeded — release lock ────────────────────────────── */
        sagaLock.release(sessionId);
        log.info("SAGA [{}]: completed, lock released. consentStatementId={}",
            sessionId, consentStatementId);

        return response;

    }
        
    /*
     * SAGA: grant consent + auditlog
     *
     * Steps:
     *   1. Acquire session lock
     *   2. Verify consent statement exists (pre-condition)
     *   3. Grant consent
     *   4. Sanity-check consent is readable
     *   5. Create auditlog
     *   6. Release lock
     */
    @Override
    public ConsentResponse grant(GrantConsentCmd cmd) {

       
        UUID sessionId = cmd.sessionId();

        /* ── Acquire session lock ───────────────────────────────────────────── */
        if (!sagaLock.tryLock(SagaConcurrencyLock.start(sessionId))) {
            throw new ConflictException("saga.already.running",
                Map.of("sessionId", sessionId.toString()));
        }
        log.info("SAGA [{}]: lock acquired, starting grant", sessionId);

        /* ── Step 1: Verify consent statement exists ───────────────────────── */
        try {
            ConsentStatementResponse statement =
                consents.getStatement(cmd.consentStatementRef());
            if (statement == null) {
                throw new ConflictException("consentstatement.not.found",
                    Map.of("id", cmd.consentStatementRef().toString()));
            }
        } catch (DomainException ex) {
            sagaLock.release(sessionId);
            throw ex;
        } catch (RuntimeException ex) {
            sagaLock.release(sessionId);
            log.warn("SAGA [{}]: consent statement get failed for {}",
                sessionId, cmd.consentStatementRef());
            throw new ConflictException("consentstatement.read.failed",
                Map.of("id", cmd.consentStatementRef().toString()));
        }

        /* ── Step 2: Grant consent ─────────────────────────────────────────── */
        ConsentResponse response;
        try {
            response = consents.grant(cmd.personRef(), cmd.consentStatementRef(),
                cmd.purpose(), cmd.type(), cmd.status());
            log.info("SAGA [{}]: consent granted {}", sessionId, response.consentId());
        } catch (DomainException ex) {
            sagaLock.release(sessionId);
            throw ex;
        } catch (RuntimeException ex) {
            sagaLock.release(sessionId);
            log.warn("SAGA [{}]: consent grant failed", sessionId);
            throw new ConflictException("consent.not.created",
                Map.of("object", "consent"));
        }

        /* ── Step 3: Sanity check ──────────────────────────────────────────── */
        try {
            ConsentResponse sanityCheck = consents.getConsent(response.consentId());
            if (sanityCheck == null) {
                sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
                throw new ConflictException("consent.not.granted",
                    Map.of("object", "consent"));
            }
        } catch (DomainException ex) {
            sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
            compensateConsentUpdate(sessionId, response.consentId());
            throw ex;
        } catch (RuntimeException ex) {
            sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
            log.warn("SAGA [{}]: consent get failed for {}, compensating",
                sessionId, response.consentId());
            compensateConsentUpdate(sessionId, response.consentId());
            throw new ConflictException("consent.read.failed",
                Map.of("id", response.consentId().toString()));
        }

        /* ── Step 4: Create auditlog ───────────────────────────────────────── */
        try {
            UUID auditlogId = auditlogs.create(
                cmd.actorRef(), cmd.actorType(), cmd.severity(),
                cmd.originSystem(), cmd.originService(), cmd.originComponent(),
                cmd.data(), cmd.description());
            log.info("SAGA [{}]: auditlog created {}", sessionId, auditlogId);
        } catch (DomainException ex) {
            sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
            compensateConsent(sessionId, response.consentId());
            throw ex;
        } catch (RuntimeException ex) {
            sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
            log.warn("SAGA [{}]: auditlog create failed, compensating consent {}",
                sessionId, response.consentId());
            compensateConsent(sessionId, response.consentId());
            throw new ConflictException("auditlog.not.created",
                Map.of("object", "auditlog"));
        }
        /* ── All steps succeeded — release lock ────────────────────────────── */
        sagaLock.release(sessionId);
        log.info("SAGA [{}]: completed, lock released. consentId={}",
            sessionId, response.consentId());
        return response;
    }

    /*
     * SAGA: withdraw consent + auditlog
     *
     * Steps:
     *   1. Acquire session lock
     *   2. Verify consent exists (pre-condition)
     *   3. Withdraw consent
     *   4. Sanity-check consent is readable after withdrawal
     *   5. Create auditlog
     *   6. Release lock
     */

    @Override
    public ConsentResponse withdraw(RevokeConsentCmd cmd) {
        UUID sessionId = cmd.sessionId();

        /* ── Acquire session lock ───────────────────────────────────────────── */
        if (!sagaLock.tryLock(SagaConcurrencyLock.start(sessionId))) {
            throw new ConflictException("saga.already.running",
                Map.of("sessionId", sessionId.toString()));
        }

        log.info("SAGA [{}]: lock acquired, starting withdraw", sessionId);

        /* ── Step 1: Verify consent exists ─────────────────────────────────── */
        try {
            ConsentResponse existing = consents.getConsent(cmd.consentId());
            if (existing == null) {
                throw new ConflictException("consent.not.found",
                    Map.of("id", cmd.consentId().toString()));
            }
        } catch (DomainException ex) {
            sagaLock.release(sessionId);
            throw ex;
        } catch (RuntimeException ex) {
            sagaLock.release(sessionId);
            log.warn("SAGA [{}]: consent get failed for {}", sessionId, cmd.consentId());
            throw new ConflictException("consent.read.failed",
                Map.of("id", cmd.consentId().toString()));
        }

        /* ── Step 2: Withdraw consent ──────────────────────────────────────── */
        ConsentResponse response;
        try {
            response = consents.withdraw(cmd.consentId());
            log.info("SAGA [{}]: consent withdrawn {}", sessionId, response.consentId());
        } catch (DomainException ex) {
            sagaLock.release(sessionId);
            throw ex;
        } catch (RuntimeException ex) {
            sagaLock.release(sessionId);
            log.warn("SAGA [{}]: consent withdraw failed for {}", sessionId, cmd.consentId());
            throw new ConflictException("consent.not.withdrawn",
                Map.of("id", cmd.consentId().toString()));
        }

        /* ── Step 3: Sanity check ──────────────────────────────────────────── */
        try {
            ConsentResponse sanityCheck = consents.getConsent(response.consentId());
            if (sanityCheck == null) {
                sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
                throw new ConflictException("consent.not.withdrawn",
                    Map.of("object", "consent"));
            }
        } catch (DomainException ex) {
            sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
            compensateConsentUpdate(sessionId, response.consentId());
            throw ex;
        } catch (RuntimeException ex) {
            sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
            log.warn("SAGA [{}]: consent get failed for {}, compensating",
                sessionId, response.consentId());
            compensateConsentUpdate(sessionId, response.consentId());
            throw new ConflictException("consent.read.failed",
                Map.of("id", response.consentId().toString()));
        }

        /* ── Step 4: Create auditlog ───────────────────────────────────────── */
        try {
            UUID auditlogId = auditlogs.create(
                cmd.actorRef(), cmd.actorType(), cmd.severity(),
                cmd.originSystem(), cmd.originService(), cmd.originComponent(),
                cmd.data(), cmd.description());
            log.info("SAGA [{}]: auditlog created {}", sessionId, auditlogId);
        } catch (DomainException ex) {
            sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
            compensateConsentUpdate(sessionId, response.consentId());
            throw ex;
        } catch (RuntimeException ex) {
            sagaLock.updateStatus(sessionId, SagaStatus.COMPENSATING);
            log.warn("SAGA [{}]: auditlog create failed, compensating consent {}",
                sessionId, response.consentId());
            compensateConsentUpdate(sessionId, response.consentId());
            throw new ConflictException("auditlog.not.created",
                Map.of("object", "auditlog"));
        }

        /* ── All steps succeeded — release lock ────────────────────────────── */
        sagaLock.release(sessionId);
        log.info("SAGA [{}]: completed, lock released. consentId={}",
            sessionId, response.consentId());

        return response;
    }

    // ── Compensation helpers ───────────────────────────────────────────────────

    private void compensateConsentStatement(UUID sessionId, UUID consentStatementId) {
        try {
            var result = consents.compensate(consentStatementId,
                this.getClass(), SagaOutcome.COMPENSATED);
            if (result.success()) {
                log.info("SAGA [{}]: consent statement {} compensated successfully",
                    sessionId, consentStatementId);
            } else {
                log.error("SAGA [{}]: compensation of statement {} FAILED — " +
                    "manual intervention required", sessionId, consentStatementId);
            }
        } catch (RuntimeException ex) {
            log.error("SAGA [{}]: compensation of statement {} threw exception — " +
                "manual intervention required: {}",
                sessionId, consentStatementId, ex.getMessage());
        } finally {
            // Always release — even if compensation fails.
            // A failed compensation is logged for manual intervention
            // but the lock must not be held forever.
            sagaLock.release(sessionId);
        }
    }

    private void compensateConsent(UUID sessionId, UUID consentId) {
        try {
            var result = consents.compensateConsent(consentId,
                this.getClass(), SagaOutcome.COMPENSATED);
            if (result.success()) {
                log.info("SAGA [{}]: consent {} compensated successfully",
                    sessionId, consentId);
            } else {
                log.error("SAGA [{}]: compensation of consent {} FAILED — " +
                    "manual intervention required", sessionId, consentId);
            }
        } catch (RuntimeException ex) {
            log.error("SAGA [{}]: compensation of consent {} threw exception — " +
                "manual intervention required: {}",
                sessionId, consentId, ex.getMessage());
        } finally {
            sagaLock.release(sessionId);
        }
    }

    private void compensateConsentUpdate(UUID sessionId, UUID consentId) {
        try {
            var result = consents.compensateConsentUpdate(consentId,
                this.getClass(), SagaOutcome.COMPENSATED);
            if (result.success()) {
                log.info("SAGA [{}]: consent update {} compensated successfully",
                    sessionId, consentId);
            } else {
                log.error("SAGA [{}]: compensation of consent update {} FAILED — " +
                    "manual intervention required", sessionId, consentId);
            }
        } catch (RuntimeException ex) {
            log.error("SAGA [{}]: compensation of consent update {} threw exception — " +
                "manual intervention required: {}",
                sessionId, consentId, ex.getMessage());
        } finally {
            sagaLock.release(sessionId);
        }
    }

}
