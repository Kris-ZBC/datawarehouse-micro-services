package local.sop.sopinfo.consent.saga.interfaceadapters.persistence.jpa;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.sagas.concurrency.locks.SagaStatus;

class ConsentSagaStateEntityTest {

    @Test
    void shouldReturnSessionIdStatusAndLockedAt() {
        UUID sessionId = UUID.randomUUID();
        SagaStatus status = SagaStatus.RUNNING;
        LocalDateTime lockedAt = LocalDateTime.now();

        ConsentSagaStateEntity entity =
                new ConsentSagaStateEntity(sessionId, status, lockedAt);

        assertEquals(sessionId, entity.getSessionId());
        assertEquals(status, entity.getStatus());
        assertEquals(lockedAt, entity.getLockedAt());
    }

    @Test
    void shouldAllowStatusToBeUpdated() {
        ConsentSagaStateEntity entity =
                new ConsentSagaStateEntity(
                        UUID.randomUUID(),
                        SagaStatus.RUNNING,
                        LocalDateTime.now()
                );

        entity.setStatus(SagaStatus.COMPENSATING);

        assertEquals(SagaStatus.COMPENSATING, entity.getStatus());
    }
}