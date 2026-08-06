package local.sop.datawarehouse.sopinstructor.interfaceadapters.persistence.jpa;

import java.time.LocalDateTime;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
;

@Entity
@Table(name = "sop_instructor")
public class SopInstructorEntity {
    @EmbeddedId
    private SopInstructorId id;

    @Version
    private Long version;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected SopInstructorEntity() {} // JPA requires a default constructor

    private SopInstructorEntity(SopInstructorId id, Boolean active) {
        this.id = id;
        this.active = active;
        this.createdAt = LocalDateTime.now();
    }

    /* wither methods */
    public SopInstructorEntity withActive(Boolean active) {
        this.active = active;
        return this;
    }

    /* getter methods */
    public SopInstructorId getId() {
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
            private SopInstructorId id;
            private Boolean active;
    
            public Builder id(SopInstructorId id) {
                this.id = id;
                return this;
            }
    
            public Builder active(Boolean active) {
                this.active = active;
                return this;
            }
    
            public SopInstructorEntity build() {

                /* Sanity checks */
                if (id == null) {
                    throw new ValidationException("key.required", Map.of("field", "id"));
                }

                /* preconditions
                * sopRef and instructorRef are validated in their respective repository adapters, so no need to validate here.
                */

                if (active == null) {
                    throw new ValidationException("sop-instructor.active.required", Map.of("field", "active" ));
                }

                return new SopInstructorEntity(id, active);
            }
        }

}
