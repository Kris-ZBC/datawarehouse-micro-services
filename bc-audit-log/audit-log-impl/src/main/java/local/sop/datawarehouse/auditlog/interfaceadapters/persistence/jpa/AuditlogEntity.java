package local.sop.datawarehouse.auditlog.interfaceadapters.persistence.jpa;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.*;

import local.sop.datawarehouse.sharedlib.enums.ActorType;
import local.sop.datawarehouse.sharedlib.enums.Severity;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.auditlog.interfaceadapters.persistence.converters.ActorTypeConverter;
import local.sop.datawarehouse.auditlog.interfaceadapters.persistence.converters.SeverityConverter;

@Entity
@Table(name = "audit_log")
public class AuditlogEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Version
    private Long version;

    @Convert(converter = ActorTypeConverter.class)
    @Column(name = "actor_type", nullable = false, columnDefinition = "ENUM('service','system','user')")
    private ActorType actorType;

    @Column(name = "actor_ref", nullable = false)
    private UUID actorRef;

    @Column(name = "origin_system", nullable = false)
    private String originSystem;

    @Column(name = "origin_service", nullable = false)
    private String originService;

    @Column(name = "origin_component", nullable = false)
    private String originComponent;

    @Convert(converter = SeverityConverter.class)
    @Column(name = "severity", nullable = false, columnDefinition = "ENUM('info','debug','low','medium','high')")
    private Severity severity;

    @Column(name = "data", nullable = false)
    private String data;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    /** JPA requirement */
    protected AuditlogEntity() {}

    private AuditlogEntity(UUID id,
                           ActorType actorType,
                           UUID actorRef,
                           String originSystem,
                           String originService,
                           String originComponent,
                           Severity severity,
                           String data,
                           String description,
                           Instant timestamp) {
        this.id = id;
        this.actorType = actorType;
        this.actorRef = actorRef;
        this.originSystem = originSystem;
        this.originService = originService;
        this.originComponent = originComponent;
        this.severity = severity;
        this.data = data;
        this.description = description;
        this.timestamp = timestamp;
    }

    /** Withers (immutable-style updates) */
	public AuditlogEntity withId(UUID id) {
		return new AuditlogEntity(id, actorType, actorRef, originSystem, originService,
				originComponent, severity, data, description, timestamp);
	}
    public AuditlogEntity withActorType(ActorType actorType) {
        return new AuditlogEntity(id, actorType, actorRef, originSystem, originService,
                originComponent, severity, data, description, timestamp);
    }

    public AuditlogEntity withActorRef(UUID actorRef) {
        return new AuditlogEntity(id, actorType, actorRef, originSystem, originService,
                originComponent, severity, data, description, timestamp);
    }

    public AuditlogEntity withOriginSystem(String originSystem) {
        return new AuditlogEntity(id, actorType, actorRef, originSystem, originService,
                originComponent, severity, data, description, timestamp);
    }

    public AuditlogEntity withOriginService(String originService) {
        return new AuditlogEntity(id, actorType, actorRef, originSystem, originService,
                originComponent, severity, data, description, timestamp);
    }

    public AuditlogEntity withOriginComponent(String originComponent) {
        return new AuditlogEntity(id, actorType, actorRef, originSystem, originService,
                originComponent, severity, data, description, timestamp);
    }

    public AuditlogEntity withSeverity(Severity severity) {
        return new AuditlogEntity(id, actorType, actorRef, originSystem, originService,
                originComponent, severity, data, description, timestamp);
    }

    public AuditlogEntity withData(String data) {
        return new AuditlogEntity(id, actorType, actorRef, originSystem, originService,
                originComponent, severity, data, description, timestamp);
    }

    public AuditlogEntity withDescription(String description) {
        return new AuditlogEntity(id, actorType, actorRef, originSystem, originService,
                originComponent, severity, data, description, timestamp);
    }

    public AuditlogEntity withTimestamp(Instant timestamp) {
        return new AuditlogEntity(id, actorType, actorRef, originSystem, originService,
                originComponent, severity, data, description, timestamp);
    }

    /** Getters */

    public UUID getId() { return id; }
    public ActorType getActorType() { return actorType; }
    public UUID getActorRef() { return actorRef; }
    public String getOriginSystem() { return originSystem; }
    public String getOriginService() { return originService; }
    public String getOriginComponent() { return originComponent; }
    public Severity getSeverity() { return severity; }
    public String getData() { return data; }
    public String getDescription() { return description; }
    public Instant getTimestamp() { return timestamp; }

    /** Builder */

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private ActorType actorType;
        private UUID actorRef;
        private String originSystem;
        private String originService;
        private String originComponent;
        private Severity severity;
        private String data;
        private String description;
        private Instant timestamp;

        public Builder id(UUID id) {
            if(id == null) throw new ValidationException("key.invalid", Map.of("field", "id"));
            this.id = id;
            return this;
        }

        public Builder actorType(ActorType actorType) {
            if (actorType == null) throw new ValidationException("actortype.invalid", Map.of("field", "actorType"));
            this.actorType = actorType;
            return this;
        }

        public Builder actorRef(UUID actorRef) {
            if (actorRef == null) throw new ValidationException("actorref.invalid", Map.of("field", "actorRef"));
            this.actorRef = actorRef;
            return this;
        }

        public Builder originSystem(String originSystem) {
            if (originSystem == null) throw new ValidationException("originsystem.invalid", Map.of("field", "originSystem"));
            this.originSystem = originSystem;
            return this;
        }

        public Builder originService(String originService) {
            if (originService == null) throw new ValidationException("originservice.invalid", Map.of("field", "originService"));
            this.originService = originService;
            return this;
        }

        public Builder originComponent(String originComponent) {
            if (originComponent == null) throw new ValidationException("origincomponent.invalid", Map.of("field", "originComponent"));
            this.originComponent = originComponent;
            return this;
        }

        public Builder severity(Severity severity) {
            if (severity == null) throw new ValidationException("severity.invalid", Map.of("field", "severity"));
            this.severity = severity;
            return this;
        }

        public Builder data(String data) {
            if (data == null) throw new ValidationException("data.invalid", Map.of("field", "data"));
            this.data = data;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            if (timestamp == null) throw new ValidationException("timestamp.invalid", Map.of("field", "timestamp"));
            this.timestamp = timestamp;
            return this;
        }

        public AuditlogEntity build() {
            return new AuditlogEntity(
                    id,
                    actorType,
                    actorRef,
                    originSystem,
                    originService,
                    originComponent,
                    severity,
                    data,
                    description,
                    timestamp
            );
        }
    }
}