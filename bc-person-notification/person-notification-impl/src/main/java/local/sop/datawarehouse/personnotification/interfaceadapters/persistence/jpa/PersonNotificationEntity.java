package local.sop.datawarehouse.personnotification.interfaceadapters.persistence.jpa;

import java.time.LocalDateTime;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

@Entity
@Table(name = "person_notification")
public class PersonNotificationEntity {

    @EmbeddedId
    private PersonNotificationId id;

    @Version
    private Long version;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected PersonNotificationEntity() {} // JPA requires a default constructor

    private PersonNotificationEntity(PersonNotificationId id, Boolean active) {
        this.id = id;
        this.active = active;
        this.createdAt = LocalDateTime.now();
    }

    /* wither methods */
    public PersonNotificationEntity withActive(Boolean active) {
        this.active = active;
        return this;
    }

    /* getters methods */

    public PersonNotificationId getId() {
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
        private PersonNotificationId id;
        private Boolean active;

        public Builder id(PersonNotificationId id) {
            this.id = id;
            return this;
        }

        public Builder active(Boolean active) {
            this.active = active;
            return this;
        }

        public PersonNotificationEntity build() {

            /* Sanity checks */
            if (id == null) {
                throw new ValidationException("key.required", Map.of("field", "id"));
            }

            /* preconditions - Expect Refs to be valid from their respective adapters */

            if (active == null) {
                throw new ValidationException("personnotification.active.required", Map.of("field", "active" ));
            }


            return new PersonNotificationEntity(id, active);
        }
    }

}
