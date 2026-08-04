package local.sop.sopinfo.educationinstructor.domain.model;

import java.util.Map;

// valueobjects
import local.sop.sopinfo.educationinstructor.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public class EducationInstructor {
    private final CompositeKey id;
    private final Boolean active;
    private final CreatedAtTimestamp createdAt;

    private EducationInstructor(CompositeKey id,Boolean active,CreatedAtTimestamp createdAt) {
        this.id = id;
        this.active = active;
        this.createdAt = createdAt;
    }

    public EducationInstructor withActive(Boolean active) {
        return new EducationInstructor(this.id,active,this.createdAt);
    }

    public EducationInstructor withCreatedAt(CreatedAtTimestamp createdAt) {
        return new EducationInstructor(this.id,this.active,createdAt);
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

        public Builder createdAt(
                CreatedAtTimestamp createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public EducationInstructor build() {

            if (active == null) {
                throw new ValidationException("educationinstructor.active.required",Map.of( "field", "active", "active",active == null ? "null" : active.toString()));
            }

            return new EducationInstructor(this.id,this.active,this.createdAt);
        }
    }
}