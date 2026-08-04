package local.sop.sopinfo.auditlog.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.sopinfo.auditlog.domain.model.Log;
import local.sop.sopinfo.auditlog.domain.model.valueobjects.LogId;
import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;

@ExtendWith(MockitoExtension.class)
class AuditlogRepositoryAdapterTest {

    @Mock
    private AuditlogSpringDataRepository jpaRepository;

    @InjectMocks
    private AuditlogRepositoryAdapter adapter;

    private Log log;
    private AuditlogEntity entity;

    @BeforeEach
    void setUp() {
        UUID id = UUID.randomUUID();

        entity = AuditlogEntity.builder()
                .id(id)
                .actorType(ActorType.USER)
                .actorRef(UUID.randomUUID())
                .originSystem("system")
                .originService("service")
                .originComponent("component")
                .severity(Severity.INFO)
                .data("data")
                .timestamp(java.time.Instant.now())
                .build();

        log = AuditlogJpaMapper.toDomain(entity);
    }

    /** -------------------------
     *  Save (with existing id)
     *  ------------------------- */
    @Test
    void save_shouldUseExistingId() {
        when(jpaRepository.save(any(AuditlogEntity.class))).thenReturn(entity);

        Log result = adapter.save(log);

        verify(jpaRepository).save(any(AuditlogEntity.class));

        assertNotNull(result);
    }

    /* * -------------------------
     *  Save (id null → generate new)
     *  ------------------------- 
    @Test
    void save_shouldGenerateIdWhenNull() {
        Log logWithoutId = mock(Log.class);
        AuditlogEntity entityWithoutId = mock(AuditlogEntity.class);

        when(entityWithoutId.getId()).thenReturn(null);

        // Mock mapper behavior via static call workaround
        // If AuditlogJpaMapper is static, you can instead verify behavior indirectly

        AuditlogEntity entityWithId = AuditlogEntity.builder()
                .id(UUID.randomUUID())
                .actorType(ActorType.USER)
                .actorRef(UUID.randomUUID())
                .originSystem("system")
                .originService("service")
                .originComponent("component")
                .severity(Severity.INFO)
                .data("data")
                .timestamp(java.time.Instant.now())
                .build();

        when(jpaRepository.save(any())).thenReturn(entityWithId);

        Log result = adapter.save(log);

        verify(jpaRepository).save(any());
        assertNotNull(result);
    } */

    /** -------------------------
     *  findAll
     *  ------------------------- */
    @Test
    void findAll_shouldReturnMappedList() {
        when(jpaRepository.findAll()).thenReturn(List.of(entity));

        List<Log> result = adapter.findAll();

        assertEquals(1, result.size());
        verify(jpaRepository).findAll();
    }

    /** -------------------------
     *  findById (found)
     *  ------------------------- */
    @Test
    void findById_shouldReturnValueWhenFound() {
        UUID id = entity.getId();

        when(jpaRepository.findById(Objects.requireNonNull(id))).thenReturn(Optional.of(entity));

        Optional<Log> result = adapter.findById(id);

        assertTrue(result.isPresent());
        verify(jpaRepository).findById(id);
    }

    /** -------------------------
     *  findById (not found)
     *  ------------------------- */
    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();

        when(jpaRepository.findById(Objects.requireNonNull(id))).thenReturn(Optional.empty());

        Optional<Log> result = adapter.findById(id);

        assertTrue(result.isEmpty());
        verify(jpaRepository).findById(id);
    }

    /** -------------------------
     *  findBySearchParams
     *  ------------------------- */
    @Test
    void findBySearchParams_shouldReturnMappedResults() {
        when(jpaRepository.findBySearchParams(
                any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(entity));

        List<Log> result = adapter.findBySearchParams(
                entity.getId(),
                entity.getActorRef(),
                ActorType.USER,
                "system",
                "service",
                "component",
                Severity.INFO
        );

        assertEquals(1, result.size());
        verify(jpaRepository).findBySearchParams(
                entity.getId(),
                entity.getActorRef(),
                ActorType.USER,
                "system",
                "service",
                "component",
                Severity.INFO
        );
    }

    @Test
    void compensate_WhenSagaOutcomeIsNotCompensate_ShouldThrowConflictException() {
        // Given
        DomainId domainId = LogId.of(entity.getId());

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            adapter.compensate(domainId, SagaOutcome.PARTIAL_FAILURE);
        });
        
        assertEquals("compensate.wrong_state", exception.getMessage());
        verify(jpaRepository, never()).deleteById(any());
    }

    @Test
    void compensate_WhenEntityDoesNotExist_ShouldReturnFalse() {
        // Given
        DomainId domainId = LogId.of(UUID.randomUUID());
        when(jpaRepository.findById(domainId.value())).thenReturn(Optional.empty());

        // When
        Boolean result = adapter.compensate(domainId, SagaOutcome.COMPENSATE);

        // Then
        assertFalse(result);
        verify(jpaRepository).findById(domainId.value());
        verify(jpaRepository, never()).deleteById(any());
    }

    @Test
    void compensate_WhenEntityExistsAndDeleteFails_ShouldReturnFalse() {
        // Given
        DomainId domainId = LogId.of(entity.getId());
        when(jpaRepository.findById(domainId.value())).thenReturn(Optional.of(entity));
        when(jpaRepository.delete(domainId.value())).thenReturn(0);

        // When
        Boolean result = adapter.compensate(domainId, SagaOutcome.COMPENSATE);

        // Then
        assertFalse(result);
        verify(jpaRepository).findById(domainId.value());
        verify(jpaRepository).delete(domainId.value());
    }

    @Test
    void compensate_WhenEntityExistsAndDeleteSucceeds_ShouldReturnTrue() {
        // Given
        DomainId domainId = LogId.of(entity.getId());
        when(jpaRepository.findById(domainId.value())).thenReturn(Optional.of(entity));
        when(jpaRepository.delete(domainId.value())).thenReturn(1);

        // When
        Boolean result = adapter.compensate(domainId, SagaOutcome.COMPENSATE);

        // Then
        assertTrue(result);
        verify(jpaRepository).findById(domainId.value());
        verify(jpaRepository).delete(domainId.value());
    }

    @Test
    void compensate_WhenSagaOutcomeIsSucceeded_ShouldThrowConflictException() {
        // Given
        DomainId domainId = LogId.of(entity.getId());

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            adapter.compensate(domainId, SagaOutcome.SUCCEEDED);
        });
        
        assertEquals("compensate.wrong_state", exception.getMessage());
        verify(jpaRepository, never()).findById(any());
        verify(jpaRepository, never()).delete(any(UUID.class));
        verify(jpaRepository, never()).deleteById(any(UUID.class));
    }
}