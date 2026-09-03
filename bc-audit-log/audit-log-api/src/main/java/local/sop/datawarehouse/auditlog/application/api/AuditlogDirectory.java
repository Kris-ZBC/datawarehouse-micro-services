package local.sop.datawarehouse.auditlog.application.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.datawarehouse.sharedlib.enums.ActorType;
import local.sop.datawarehouse.sharedlib.enums.Severity;
import local.sop.common.libs.sharedkernel.sagas.compensate.Compensatable;
import local.sop.datawarehouse.auditlog.application.api.dto.*;

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
