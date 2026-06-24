package local.sop.sopinfo.auditlog.interfaceadapters.persistence.jpa;
import local.sop.sopinfo.auditlog.domain.model.Log;
import local.sop.sopinfo.auditlog.domain.model.valueobjects.*;

public class AuditlogJpaMapper {
    private AuditlogJpaMapper() {}

    public static Log toDomain(AuditlogEntity entity) {
        return Log.builder()
            .id(new LogId(entity.getId()))
            .actorRef(new ActorRef(entity.getActorRef()))
            .actorType(entity.getActorType())
            .originSystem(new OriginSystem(entity.getOriginSystem()))
            .originService(new OriginService(entity.getOriginService()))
            .originComponent(new OriginComponent(entity.getOriginComponent()))
            .severity(entity.getSeverity())
            .data(new Data(entity.getData()))
            .description(entity.getDescription() == null ? null : new Description(entity.getDescription()))
            .timestamp(new LogTimestamp(entity.getTimestamp()))
            .build();
    }

    public static AuditlogEntity toEntity(Log log) {
        return new AuditlogEntity.Builder()
            .id(log.getId().value())
            .actorRef(log.getActorRef().value())
            .actorType(log.getActorType())
            .originSystem(log.getOriginSystem().value())
            .originService(log.getOriginService().value())
            .originComponent(log.getOriginComponent().value())
            .severity(log.getSeverity())
            .data(log.getData().json())
            .description(log.getDescription() == null ? null : log.getDescription().value())
            .timestamp(log.getTimestamp().value())
            .build();
    }
}
