package local.sop.sopinfo.education.saga.application.ports.out.saga;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.concurrency.locks.SagaStatus;
import local.sop.common.libs.sharedkernel.sagas.concurrency.locks.SagaConcurrencyLock;

public interface EducationSagaStatePort {
    
    /**
     * Attempt to acquire a SAGA lock for this sessionId.
     * Returns false if a lock already exists — concurrent/duplicate request detected.
     */
    boolean tryLock(SagaConcurrencyLock lock);

    /**
     * Update the lock status — e.g. RUNNING → COMPENSATING.
     */
    void updateStatus(UUID sessionId, SagaStatus status);

    /**
     * Release the lock — called when SAGA completes or is fully compensated.
     * Deletes the row entirely — lock table is a temporary mutex, not an audit log.
     */
    void release(UUID sessionId);

    /**
     * Check if a lock exists for this sessionId.
     */
    boolean isLocked(UUID sessionId);
}
