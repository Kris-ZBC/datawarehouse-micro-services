package local.sop.sopinfo.message.saga.application.ports.out.saga;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.sagas.concurrency.locks.SagaConcurrencyLock;
import local.sop.sopinfo.sharedkernel.sagas.concurrency.locks.SagaStatus;

public interface MessageSagaStatePort {

	boolean tryLock(SagaConcurrencyLock lock);
	void updateStatus(UUID sessionId, SagaStatus status);
	void release(UUID sessionId);
	boolean isLocked(UUID sessionId);
}
