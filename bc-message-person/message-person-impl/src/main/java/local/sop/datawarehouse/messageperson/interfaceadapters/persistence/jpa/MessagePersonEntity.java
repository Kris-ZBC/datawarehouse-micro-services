package local.sop.datawarehouse.messageperson.interfaceadapters.persistence.jpa;

import java.time.LocalDateTime;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

@Entity
@Table(name = "message_person")
public class MessagePersonEntity {
    @EmbeddedId
    private MessagePersonId id;

    @Version
    private Long version;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected MessagePersonEntity() {} // JPA requires a default constructor

    private MessagePersonEntity(MessagePersonId id, Boolean active) {
        this.id = id;
        this.active = active;
        this.createdAt = LocalDateTime.now();
    }

    /* wither methods */
    public MessagePersonEntity withActive(Boolean active) {
        this.active = active;
        return this;
    }

    /* getter methods */
    public MessagePersonId getId() {
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
            private MessagePersonId id;
            private Boolean active;
    
            public Builder id(MessagePersonId id) {
                this.id = id;
                return this;
            }
    
            public Builder active(Boolean active) {
                this.active = active;
                return this;
            }
    
            public MessagePersonEntity build() {

                /* Sanity checks */
                if (id == null) {
                    throw new ValidationException("key.required", Map.of("field", "id"));
                }

                /* preconditions
                * messageRef and personRef are validated in their respective repository adapters, so no need to validate here.
                */

                if (active == null) {
                    throw new ValidationException("message-person.active.required", Map.of("field", "active" ));
                }

                return new MessagePersonEntity(id, active);
            }
        }

}
