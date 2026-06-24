package local.sop.sopinfo.instructor.interfaceadapters.persistence.jpa;

import java.util.Map;
import java.util.UUID;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import local.sop.sopinfo.instructor.domain.model.valueobjects.InstructorId;
import local.sop.sopinfo.instructor.domain.model.valueobjects.PersonRef;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

@Entity
@Table(name = "instructor")
public class InstructorEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "person_ref", nullable = false)
    private UUID personRef;

    @Version
    private Long version;

    private InstructorEntity(UUID id, UUID personRef) {
        this.id = id;
        this.personRef = personRef;
    }

    protected InstructorEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPersonRef() {
        return personRef;
    }

    public void setPersonRef(UUID personRef) {
        this.personRef = personRef;
    }

    public Long getVersion() {
        return version;
    }

    public static final class Builder {
        private InstructorId id;
        private PersonRef personRef;

        public Builder id(InstructorId value) {
            this.id = value;
            return this;
        }

        public Builder personRef(PersonRef value) {
            this.personRef = value;
            return this;
        }

        public InstructorEntity Build() {
            if (id == null) {
                id = InstructorId.newId();
            }

            if (personRef == null) {
                throw new ValidationException(
                        "instructor.personRef.required",
                        Map.of("field", "personRef")
                );
            }

            return new InstructorEntity(id.value(), personRef.value());
        }
    }
}