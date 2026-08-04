package local.sop.sopinfo.consent.saga.interfaceadapters.persistence.jpa;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import local.sop.sopinfo.consent.saga.application.ports.out.saga.ConsentSagaStatePort;
import local.sop.common.libs.sharedkernel.sagas.concurrency.locks.SagaStatus;
import local.sop.common.libs.sharedkernel.sagas.concurrency.locks.SagaConcurrencyLock;

@Component
public class ConsentSagaStateRepositoryAdapter implements ConsentSagaStatePort {

    private final ConsentSagaStateSpringDataRepository repository;

    public ConsentSagaStateRepositoryAdapter(ConsentSagaStateSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean tryLock(SagaConcurrencyLock lock) {
        try {
            repository.saveAndFlush(new ConsentSagaStateEntity(
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