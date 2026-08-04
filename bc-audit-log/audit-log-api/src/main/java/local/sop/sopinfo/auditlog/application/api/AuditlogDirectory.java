package local.sop.sopinfo.auditlog.application.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.auditlog.application.api.dto.*;
import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.common.libs.sharedkernel.sagas.compensate.Compensatable;

public interface AuditlogDirectory extends Compensatable {

    CreatedAuditlogResponse createAuditlog(CreateAuditlogCmd cmd);

    List<AuditlogResponse> findAll();

    List<AuditlogResponse> findBySearchParams(UUID id,
                                              UUID actorRef,
                                              ActorType actorType,
                                              Severity severity,
                                              String originSystem,
                                              String originService,
                                              String originComponent);
    
    Optional<AuditlogResponse> findById(UUID id);
    

}
