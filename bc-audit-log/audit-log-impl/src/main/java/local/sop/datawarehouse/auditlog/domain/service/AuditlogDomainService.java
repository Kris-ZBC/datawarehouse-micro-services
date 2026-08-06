package local.sop.datawarehouse.auditlog.domain.service;

import local.sop.datawarehouse.auditlog.domain.model.Log;
import local.sop.datawarehouse.auditlog.domain.model.valueobjects.LogId;
import local.sop.datawarehouse.auditlog.domain.model.valueobjects.LogTimestamp;
public class AuditlogDomainService implements AuditLogDomain{

    @Override
    public Log createLog(Log log) {
        // Domain-specific logic can be added here if needed in the future
        return Log.builder()
                .id(LogId.newId())
                .actorRef(log.getActorRef())
                .actorType(log.getActorType())
                .severity(log.getSeverity())
                .originSystem(log.getOriginSystem())
                .originService(log.getOriginService())
                .originComponent(log.getOriginComponent())
                .data(log.getData())
                .description(log.getDescription())
                .timestamp(LogTimestamp.now())
                .build();
    }
}
