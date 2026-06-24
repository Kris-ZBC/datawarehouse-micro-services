package local.sop.sopinfo.education.domain.model;

import java.util.Map;

import local.sop.sopinfo.education.domain.model.valueobjects.EducationCategory;
import local.sop.sopinfo.education.domain.model.valueobjects.EducationId;
import local.sop.sopinfo.education.domain.model.valueobjects.EducationName;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public class Education {

    private final EducationId id;
    private final EducationName name;
    private final EducationCategory category;
    private final Boolean active;

    public Education(EducationId id, EducationName name, EducationCategory category, Boolean active) {
        this.id = requireNonNull(id, "education.id.required", "educationId");
        this.name = requireNonNull(name, "education.name.required", "educationName");
        this.category = requireNonNull(category, "education.category.required", "educationCategory");
        this.active = requireNonNull(active, "education.active.required", "active");
    }

    public static Education create(EducationId id, EducationName name, EducationCategory category, Boolean active) {
        EducationId actualId = id != null ? id : EducationId.newId();
        Boolean actualActive = active != null ? active : false;
        return new Education(actualId, name, category, actualActive);
    }

    public EducationId getId() {
        return id;
    }

    public EducationName getName() {
        return name;
    }

    public EducationCategory getCategory() {
        return category;
    }

    public Boolean isActive() {
        return active;
    }

    public static Builder builder() {
        return new Builder();
    }

    private static <T> T requireNonNull(T value, String messageKey, String field) {
        if (value == null) {
            throw new ValidationException(messageKey, Map.of("field", field));
        }
        return value;
    }

    public static class Builder {
        private EducationId id;
        private EducationName name;
        private EducationCategory category;
        private Boolean active;

        public Builder id(EducationId id) {
            this.id = id;
            return this;
        }

        public Builder name(EducationName name) {
            this.name = name;
            return this;
        }

        public Builder category(EducationCategory category) {
            this.category = category;
            return this;
        }

        public Builder active(Boolean active) {
            this.active = active;
            return this;
        }

        public Education build() {
            return Education.create(id, name, category, active);
        }
    }
}