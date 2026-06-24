package local.sop.sopinfo.sopeducation.interfaceadapters.persistence.jpa;

import java.time.LocalDateTime;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

@Entity
@Table(name = "sop_education")
public class SopEducationEntity {
    @EmbeddedId
    private SopEducationId id;

    @Version
    private Long version;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column (name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected SopEducationEntity() {} // JPA requires a default constructor 

    private SopEducationEntity(SopEducationId id, Boolean active) {
        this.id = id;
        this.active = active;
        this.createdAt = LocalDateTime.now();
    }

    /* wither methods */
    public SopEducationEntity withActive(Boolean active) {
        this.active = active;
        return this;
    }

    /* getter methods */
    public SopEducationId getId() {
        return id;
    }
    public Boolean isActive() {
        return active;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }   

    /* Builder factory inner class */
    public static class Builder {
        private SopEducationId id;
        private Boolean active;

        public Builder id(SopEducationId id) {
            this.id = id;
            return this;
        }

        public Builder active(Boolean active) {
            this.active = active;
            return this;
        }

        public SopEducationEntity build() {
            
            /* sanity checks */
            if (id == null) {
                throw new ValidationException("key.required", Map.of("field", "id"));
            }

            /* preconditions 
             * sopRef and educationLineRef are validated in the SopEducationLine usecase servicen, so no need to validate here
            */

            if (active == null) {
                throw new ValidationException("sop-education.active.required", Map.of("field", "active"));
            }

            return new SopEducationEntity(id, active);
        }
    }

}