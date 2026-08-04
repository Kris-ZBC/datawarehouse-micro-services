package local.sop.sopinfo.consent.saga.interfaceadapters.persistence.jpa;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import local.sop.common.libs.sharedkernel.sagas.concurrency.locks.SagaConcurrencyLock;
import local.sop.common.libs.sharedkernel.sagas.concurrency.locks.SagaStatus;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentSagaStateRepositoryAdapterTest {

    @Mock
    private ConsentSagaStateSpringDataRepository repository;

    private ConsentSagaStateRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ConsentSagaStateRepositoryAdapter(repository);
    }

    @Test
    void shouldUpdateStatus_whenEntityExists() {
        UUID sessionId = UUID.randomUUID();

        var entity = mock(ConsentSagaStateEntity.class);

        when(repository.findById(sessionId))
                .thenReturn(Optional.of(entity));

        adapter.updateStatus(sessionId, SagaStatus.COMPENSATING);

        verify(entity).setStatus(SagaStatus.COMPENSATING);
        verify(repository).save(entity);
    }

    @Test
    void shouldDoNothing_whenEntityNotFound() {
        UUID sessionId = UUID.randomUUID();

        when(repository.findById(sessionId))
                .thenReturn(Optional.empty());

        adapter.updateStatus(sessionId, SagaStatus.COMPENSATING);

        verify(repository, never()).save(any());
    }

    @Test
    void shouldDeleteById_whenReleaseCalled() {
        UUID sessionId = UUID.randomUUID();

        adapter.release(sessionId);

        verify(repository).deleteById(sessionId);
    }

    @Test
    void shouldReturnTrue_whenExistsByIdReturnsTrue() {
        UUID sessionId = UUID.randomUUID();

        when(repository.existsById(sessionId)).thenReturn(true);

        boolean result = adapter.isLocked(sessionId);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalse_whenExistsByIdReturnsFalse() {
        UUID sessionId = UUID.randomUUID();

        when(repository.existsById(sessionId)).thenReturn(false);

        boolean result = adapter.isLocked(sessionId);

        assertFalse(result);
    }

    @Test
    void tryLock_shouldReturnTrue_whenSaveAndFlushSucceeds() {
        SagaConcurrencyLock lock = new SagaConcurrencyLock(
                UUID.randomUUID(),
                SagaStatus.RUNNING,
                LocalDateTime.now());

        boolean result = adapter.tryLock(lock);

        assertTrue(result);
        verify(repository).saveAndFlush(any());
    }

    @Test
    void tryLock_shouldReturnFalse_whenDataIntegrityViolationOccurs() {
        SagaConcurrencyLock lock = new SagaConcurrencyLock(
                UUID.randomUUID(),
                SagaStatus.RUNNING,
                LocalDateTime.now());

        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(repository).saveAndFlush(any());

        boolean result = adapter.tryLock(lock);

        assertFalse(result);
        verify(repository).saveAndFlush(any());
    }
    
}