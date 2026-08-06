package local.sop.datawarehouse.education.saga.interfaceadapters.persistence.jpa;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;

import local.sop.common.libs.sharedkernel.sagas.concurrency.locks.SagaConcurrencyLock;
import local.sop.common.libs.sharedkernel.sagas.concurrency.locks.SagaStatus;
import local.sop.datawarehouse.education.saga.application.ports.out.saga.EducationSagaStatePort;

public class EducationSagaStateRepositoryAdapter implements EducationSagaStatePort {

    private final EducationSagaStateSpringDataRepository repository;

    public EducationSagaStateRepositoryAdapter(EducationSagaStateSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean tryLock(SagaConcurrencyLock lock) {
        try {
            repository.saveAndFlush(new EducationSagaStateEntity(
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
