package local.sop.sopinfo.messageperson.domain.model;

/* Aggregate root for MessagePerson */

import java.util.Map;

import local.sop.sopinfo.messageperson.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public class MessagePerson {
    private final CompositeKey id;
    private final Boolean active;
    private final CreatedAtTimestamp createdAt;

    private MessagePerson(CompositeKey id, Boolean active, CreatedAtTimestamp createdAt) {
        this.id = id;
        this.active = active;
        this.createdAt = createdAt;
    }

    public MessagePerson withActive(Boolean active) {
        return new MessagePerson(this.id, active, this.createdAt);
    }

    public MessagePerson withCreatedAt(CreatedAtTimestamp createdAt) {
        return new MessagePerson(this.id, this.active, createdAt);
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

        /* Post condition
            * id and creadAt are validated in their respective value objects, so no need to validate here.
        */
        public MessagePerson build() {
                if(active == null) {
                    throw new ValidationException("message-person.active.required",Map.of("field", "active", "active", active ==null? "null" : active.toString()));
                }

                return new MessagePerson(this.id, this.active, this.createdAt);
        }

    }

}