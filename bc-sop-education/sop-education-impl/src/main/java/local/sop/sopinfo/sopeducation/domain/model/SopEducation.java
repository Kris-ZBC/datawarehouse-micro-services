package local.sop.sopinfo.sopeducation.domain.model;

/* Aggregate root for SopEducation */

import java.util.Map;

import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.sopeducation.domain.model.valueobjects.CreatedAtTimestamp;


public class SopEducation {
    private final CompositeKey id;
    private final Boolean active;
    private final CreatedAtTimestamp createdAt;

    private SopEducation(CompositeKey id, Boolean active, CreatedAtTimestamp createdAt) {
        this.id = id;
        this.active = active;
        this.createdAt = createdAt;
    }

    public SopEducation withActive(Boolean active) {
        return new SopEducation(this.id, active, this.createdAt);
    }

    public SopEducation withCreatedAt(CreatedAtTimestamp createdAt) {
        return new SopEducation(this.id, this.active, createdAt);
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

    /* Builder factory inner class */
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
            * id and createdAt are validated in their respective value objects, so no need to validate here
         */
        public SopEducation build() {

            if(active == null) {
                throw new ValidationException("sop-education.active.required", Map.of("field", "active", "active", active ==null? "null" : active.toString()));
            }

            return new SopEducation(this.id, this.active, this.createdAt);
        }
    }

}
