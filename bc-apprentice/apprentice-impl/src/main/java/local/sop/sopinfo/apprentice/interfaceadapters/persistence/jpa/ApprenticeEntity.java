package local.sop.sopinfo.apprentice.interfaceadapters.persistence.jpa;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import local.sop.sopinfo.apprentice.interfaceadapters.persistence.jpa.converters.UUIDBinaryConverter;

@Entity
@Table(name = "apprentice", schema = "apprentice")
public class ApprenticeEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Version
    private Long version;

    @Column(name = "person_ref", columnDefinition = "BINARY(16)", nullable = false)
    @Convert(converter = UUIDBinaryConverter.class)
    private UUID personRef;

    @Column(name = "education_line_ref", columnDefinition = "BINARY(16)", nullable = false)
    @Convert(converter = UUIDBinaryConverter.class)
    private UUID educationLineRef;

    // JPA default constructor (public so tests can use it)
    public ApprenticeEntity() {}

    public ApprenticeEntity(UUID id, UUID personRef, UUID educationLineRef) {
        this.id = id;
        this.personRef = personRef;
        this.educationLineRef = educationLineRef;
    }

    public UUID getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public UUID getPersonRef() {
        return personRef;
    }

    public UUID getEducationLineRef() {
        return educationLineRef;
    }


    public ApprenticeEntity withId(UUID id) {
        return new ApprenticeEntity(id, this.personRef, this.educationLineRef);
    }

    public ApprenticeEntity withPersonRef(UUID personRef) {
        return new ApprenticeEntity(this.id, personRef, this.educationLineRef);
    }

    public ApprenticeEntity withEducationLineRef(UUID educationLineRef) {
        return new ApprenticeEntity(this.id, this.personRef, educationLineRef);
    }
}