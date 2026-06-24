package local.sop.sopinfo.sharedkernel.sagas.concurrency.locks;

import java.time.LocalDateTime;
import java.util.UUID;

public record SagaConcurrencyLock(
   UUID sessionId,
    SagaStatus status,
    LocalDateTime lockedAt
) {
    public static SagaConcurrencyLock start(UUID sessionId) {
        return new SagaConcurrencyLock(sessionId, SagaStatus.RUNNING, LocalDateTime.now());
    }

    public SagaConcurrencyLock compensating() {
        return new SagaConcurrencyLock(sessionId, SagaStatus.COMPENSATING, lockedAt);
    }

}
