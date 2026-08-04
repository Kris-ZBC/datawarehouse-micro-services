package local.sop.sopinfo.auditlog.domain.model;

import java.util.Map;

import local.sop.sopinfo.auditlog.domain.model.valueobjects.Data;
import local.sop.sopinfo.auditlog.domain.model.valueobjects.Description;
import local.sop.sopinfo.auditlog.domain.model.valueobjects.LogId;
import local.sop.sopinfo.auditlog.domain.model.valueobjects.LogTimestamp;
import local.sop.sopinfo.auditlog.domain.model.valueobjects.OriginComponent;
import local.sop.sopinfo.auditlog.domain.model.valueobjects.OriginService;
import local.sop.sopinfo.auditlog.domain.model.valueobjects.OriginSystem;
import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;

/**
 * Aggregate root representing an audit log entry.
 */
public class Log {

    private final DomainId id;
    private final DomainId actorRef;
    private final ActorType actorType;
    private final Severity severity;
    private final OriginSystem originSystem;
    private final OriginService originService;
    private final OriginComponent originComponent;
    private final Data data;
    private final Description description;
    private final LogTimestamp timestamp;

    private Log(DomainId id, DomainId actorRef, ActorType actorType, Severity severity, OriginSystem originSystem, OriginService originService, OriginComponent originComponent, Data data, Description description, LogTimestamp timestamp) {
        this.id = id;
        this.actorRef = actorRef;
        this.actorType = actorType;
        this.severity = severity;
        this.originSystem = originSystem;
        this.originService = originService;
        this.originComponent = originComponent;
        this.data = data;
        this.description = description; // optional
        this.timestamp = timestamp;
    }

    public DomainId getId() {
        return id;
    }

    public Severity getSeverity() {
        return severity;
    }

    public DomainId getActorRef() {
        return actorRef;
    }

    public ActorType getActorType() {
        return actorType;
    }

    public OriginSystem getOriginSystem() {
        return originSystem;
    }   

    public OriginService getOriginService() {
        return originService;
    }

    public OriginComponent getOriginComponent() {
        return originComponent;
    }

    public Data getData() {
        return data;
    }

    public Description getDescription() {
        return description;
    }

    public LogTimestamp getTimestamp() {
        return timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DomainId id;
        private DomainId actorRef;
        private ActorType actorType;
        private Severity severity;
        private OriginSystem originSystem;
        private OriginService originService;
        private OriginComponent originComponent;
        private Data data;
        private Description description;
        private LogTimestamp timestamp;

        public Builder id(DomainId id) { this.id = id; return this;}
        public Builder actorRef(DomainId actorRef) {this.actorRef = actorRef; return this;}
        public Builder actorType(ActorType actorType) {this.actorType = actorType;return this;}
        public Builder severity(Severity severity) { this.severity = severity;return this;}
        public Builder originSystem(OriginSystem originSystem) {this.originSystem = originSystem; return this;}
        public Builder originService(OriginService originService) {this.originService = originService;return this;}
        public Builder originComponent(OriginComponent originComponent) {this.originComponent = originComponent;return this;}
        public Builder data(Data data) {this.data = data;return this;}
        public Builder description(Description description) {this.description = description;return this;}
        public Builder timestamp(LogTimestamp timestamp) {this.timestamp = timestamp;return this;}

        public Log build() {
			if (id == null){this.id = LogId.newId();}
			if (actorRef == null){throw new ValidationException("key.required", Map.of("field", "actorRef"));}
			if (actorType == null){throw new ValidationException("log.actorType.required", Map.of("field", "actorType"));}
			if (severity == null){throw new ValidationException("log.severity.required", Map.of("field", "severity"));}
			if (originSystem == null){throw new ValidationException("log.origin.system.required", Map.of("field", "originSystem"));}
			if (originService == null){throw new ValidationException("log.origin.service.required", Map.of("field", "originService"));}
			if (originComponent == null){throw new ValidationException("log.origin.component.required", Map.of("field", "originComponent"));}
			if (data == null){throw new ValidationException("log.data.required", Map.of("field", "data"));}
			if (timestamp == null){throw new ValidationException("log.timestamp.required", Map.of("field", "timestamp"));}

             return new Log(id, actorRef, actorType, severity, originSystem, originService, originComponent, data, description, timestamp);
        }
    }
}
	