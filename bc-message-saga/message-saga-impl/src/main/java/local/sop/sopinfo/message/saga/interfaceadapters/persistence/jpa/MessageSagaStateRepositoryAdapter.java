package local.sop.sopinfo.message.saga.interfaceadapters.persistence.jpa;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import local.sop.sopinfo.message.saga.application.ports.out.saga.MessageSagaStatePort;
import local.sop.sopinfo.sharedkernel.sagas.concurrency.locks.SagaConcurrencyLock;
import local.sop.sopinfo.sharedkernel.sagas.concurrency.locks.SagaStatus;

@Component
public class MessageSagaStateRepositoryAdapter implements MessageSagaStatePort {

	private final MessageSagaStateSpringDataRepository repository;

	public MessageSagaStateRepositoryAdapter(MessageSagaStateSpringDataRepository repository) {
		this.repository = repository;
	}

	@Override
	public boolean tryLock(SagaConcurrencyLock lock) {
		try {
			repository.saveAndFlush(new MessageSagaStateEntity(
				lock.sessionId(), lock.status(), lock.lockedAt()));
			return true;
		} catch (DataIntegrityViolationException ex) {
			// Duplicate primary key — concurrent request for same sessionId
			return false;
		}
	}

	@Override
	public void updateStatus(UUID sessionId, SagaStatus status) {
		repository.findById(sessionId).ifPresent(entity -> {
			entity.setStatus(status);
			repository.save(entity);
		});
	}

	@Override 
	public void release(UUID sessionId) {
		repository.deleteById(sessionId);
	}

	@Override
	public boolean isLocked(UUID sessionId) {
		return repository.existsById(sessionId);
	}
}
