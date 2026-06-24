package local.sop.sopinfo.sopinstructor.domain.model;

import java.util.Map;

import local.sop.sopinfo.sopinstructor.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public class SopInstructor {
    private final CompositeKey id;
    private final Boolean active;
    private final CreatedAtTimestamp createdAt;

    private SopInstructor(CompositeKey id, Boolean active, CreatedAtTimestamp createdAt) {
        this.id = id;
        this.active = active;
        this.createdAt = createdAt;
    }

    public SopInstructor withActive(Boolean active) {
        return new SopInstructor(this.id, active, this.createdAt);
    }

    public SopInstructor withCreatedAt(CreatedAtTimestamp createdAt) {
        return new SopInstructor(this.id, this.active, createdAt);
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

        /* Post conditions:
         * - id must not be null
         * - active must not be null
         * - createdAt must not be null
         * Thus no need to validate here.
         */

        public SopInstructor build() {
                if(active == null) {
                    throw new ValidationException("sop-instructor.active.required",Map.of("field", "active", "active", active ==null? "null" : active.toString()));
                }

                return new SopInstructor(this.id, this.active, this.createdAt);
        }

    }

}