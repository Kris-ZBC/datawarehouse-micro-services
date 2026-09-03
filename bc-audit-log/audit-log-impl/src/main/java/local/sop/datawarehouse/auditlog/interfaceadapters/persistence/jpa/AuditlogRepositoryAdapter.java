package local.sop.datawarehouse.auditlog.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import local.sop.datawarehouse.sharedlib.enums.ActorType;
import local.sop.datawarehouse.sharedlib.enums.Severity;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;
import local.sop.datawarehouse.auditlog.domain.model.Log;
import local.sop.datawarehouse.auditlog.domain.ports.out.AuditlogRepositoryPort;

@Repository
@Qualifier("JpaAuditlogRepository")
public class AuditlogRepositoryAdapter implements AuditlogRepositoryPort {
    private final AuditlogSpringDataRepository jpaRepository;

    public AuditlogRepositoryAdapter(AuditlogSpringDataRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Log save(Log Log) {
        AuditlogEntity entity = AuditlogJpaMapper.toEntity(Log);
        AuditlogEntity savedEntity = jpaRepository.save(Objects.requireNonNull(entity));
        return AuditlogJpaMapper.toDomain(savedEntity);
    }

    @Override
    public List<Log> findAll() {
        List<AuditlogEntity> entities = jpaRepository.findAll();
        return entities.stream()
                   .map(AuditlogJpaMapper::toDomain)
                   .toList();
    }

    @Override
    public List<Log> findBySearchParams(UUID id, UUID actorRef, ActorType actorType, String originSystem, String originService, String originComponent, Severity severity) {
        List<AuditlogEntity> entities = jpaRepository.findBySearchParams(id, actorRef, actorType, originSystem, originService, originComponent, severity);
        return entities.stream()
                   .map(AuditlogJpaMapper::toDomain)
                   .toList();
     }

    @Override
    public Optional<Log> findById(UUID id) {
        Optional<AuditlogEntity> entityOpt = jpaRepository.findById(Objects.requireNonNull(id));
        return entityOpt.map(AuditlogJpaMapper::toDomain);
    }
    
    @Override
    public Boolean compensate(DomainId id, SagaOutcome sagaState) {
        if(sagaState != SagaOutcome.COMPENSATE)
            throw new ConflictException("compensate.wrong_state", Map.of("compensate", sagaState.name()));
        var found = findById(id.value());
        if(found == null) {
            return false;
        }
        return (jpaRepository.delete(id.value()) == 1? true: false);
    }
}
