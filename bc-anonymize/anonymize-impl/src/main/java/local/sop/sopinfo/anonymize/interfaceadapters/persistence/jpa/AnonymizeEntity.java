package local.sop.sopinfo.anonymize.interfaceadapters.persistence.jpa;

import java.util.UUID;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

@Entity
@Table(name = "anonymization")
public class AnonymizeEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "person_ref", nullable = false, updatable = false)
    private UUID personRef;

    @Version
    private Long version;

    protected AnonymizeEntity() { } //JPA requires a default constructor

    public UUID getId() {
        return id;
    }


    public UUID getPersonRef() {
        return personRef;
    }

    public Long getVersion() {
        return version;
    }

    /* Builder factory */
    public static AnonymizeEntity create(UUID id, UUID personRef) {
        AnonymizeEntity entity = new AnonymizeEntity();
        if(id == null) {
            throw new ValidationException("key.invalid", Map.of("field", "id"));
        }
        if(personRef == null) {
            throw new ValidationException("anonymization.personRef.invalid", Map.of("field", "personRef"));
        }
        entity.id = id;
        entity.personRef = personRef;
        return entity;
    }
}