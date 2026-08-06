package local.sop.datawarehouse.personnotification.domain.model;

import java.util.Map;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.personnotification.domain.model.valueobjects.CreatedAtTimestamp;

public class PersonNotification {
    private final CompositeKey id;
    private final Boolean active;
    private final CreatedAtTimestamp createdAt;

    private PersonNotification(CompositeKey id, Boolean active, CreatedAtTimestamp createdAt) {
        this.id = id;
        this.active = active;
        this.createdAt = createdAt;
    }

    public PersonNotification withActive(Boolean active) {
        return new PersonNotification(this.id, active, this.createdAt);
    }

    public PersonNotification withCreatedAt(CreatedAtTimestamp createdAt) {
        return new PersonNotification(this.id, this.active, createdAt);
    }

    public CompositeKey getId() {
        return id;
    }

    public Boolean isActive() {
        return active;
    }

    public CreatedAtTimestamp getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CompositeKey id;
        private Boolean active;
        private CreatedAtTimestamp createdAt;

        public Builder id(CompositeKey id) {
            this.id = id;
            return this;
        }

        public Builder active(Boolean active) {
            this.active = active;
            return this;
        }

        public Builder createdAt(CreatedAtTimestamp createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PersonNotification build() {
            if (active == null) {
                throw new ValidationException("personnotification.active.required",Map.of("field", "active", "active", active ==null? "null" : active.toString()));
            }

            

            return new PersonNotification(this.id, this.active, this.createdAt);
        }
    }
}   
