package local.sop.sopinfo.instructor.domain.model;

import java.util.Map;
import java.util.StringJoiner;

import local.sop.sopinfo.instructor.domain.model.valueobjects.InstructorId;
import local.sop.sopinfo.instructor.domain.model.valueobjects.PersonRef;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;

/**
 * Aggregate Root: Instructor
 */
public final class Instructor {

    private final DomainId id;
    private final DomainId personRef;

    private Instructor(DomainId id, DomainId personRef) {
        this.id = id;
        this.personRef = personRef;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Instructor create(PersonRef personRef) {
        return builder()
                .personRef(personRef)
                .build();
    }

    public Instructor withPersonRef(DomainId newPersonRef) {
        return Instructor.builder()
                .id(this.id)
                .personRef(newPersonRef)
                .build();
    }

    public DomainId getId() {
        return id;
    }

    public DomainId getPersonRef() {
        return personRef;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "{", "}")
                .add("id=" + (id == null ? null : id.toString()))
                .add("personRef=" + (personRef == null ? null : personRef.toString()))
                .toString();
    }

    public static final class Builder {
        private DomainId id;
        private DomainId personRef;

        public Builder id(DomainId value) {
            this.id = value;
            return this;
        }

        public Builder personRef(DomainId value) {
            this.personRef = value;
            return this;
        }

        public Instructor build() {
            if (id == null) {
                id = InstructorId.newId();
            }

            if (personRef == null) {
                throw new ValidationException(
                        "key.required",
                        Map.of("field", "personRef")
                );
            }

            return new Instructor(id, personRef);
        }
    }
}