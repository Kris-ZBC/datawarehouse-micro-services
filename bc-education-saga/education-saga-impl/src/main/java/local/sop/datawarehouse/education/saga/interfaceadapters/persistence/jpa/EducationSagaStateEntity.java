package local.sop.datawarehouse.education.saga.interfaceadapters.persistence.jpa;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import local.sop.common.libs.sharedkernel.sagas.concurrency.locks.SagaStatus;

@Entity
@Table(name = "education_saga_state")
public class EducationSagaStateEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID sessionId;
    
    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime lockedAt;

    protected EducationSagaStateEntity() {}

    public EducationSagaStateEntity(UUID sessionId, SagaStatus status,
            LocalDateTime lockedAt) {
        this.sessionId = sessionId;
        this.status = status;
        this.lockedAt = lockedAt;
    }

    public UUID getSessionId() { return sessionId; }
    public SagaStatus getStatus() { return status; }
    public LocalDateTime getLockedAt() { return lockedAt; }
    public void setStatus(SagaStatus status) { this.status = status; }
}