package local.sop.sopinfo.auditlog.domain.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.auditlog.domain.model.Log;
import local.sop.sopinfo.sharedkernel.enums.ActorType;
import local.sop.sopinfo.sharedkernel.enums.Severity;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;

public interface AuditlogRepositoryPort {
    Log save(Log log);
 
    List<Log> findAll();
 
    List<Log> findBySearchParams( UUID id, UUID actorRef, ActorType actorType, String originSystem, String originService, String originComponent, Severity severity);

    Optional<Log> findById(UUID id);

    Boolean compensate(DomainId id, SagaOutcome sagaState);
}
