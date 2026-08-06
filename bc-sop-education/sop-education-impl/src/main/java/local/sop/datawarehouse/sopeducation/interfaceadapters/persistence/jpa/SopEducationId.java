package local.sop.datawarehouse.sopeducation.interfaceadapters.persistence.jpa;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class SopEducationId implements Serializable {
    @Column(name = "sop_ref", nullable = false)
    private UUID sopRef;

    @Column(name = "education_ref", nullable = false)
    private UUID educationRef;

    protected SopEducationId() {} // JPA requires a default constructor

    public SopEducationId(UUID sopRef, UUID educationRef) {
        this.sopRef = sopRef;
        this.educationRef = educationRef;
    }

    public UUID getSopRef() {
        return sopRef;
    }

    public UUID getEducationRef() {
        return educationRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SopEducationId that = (SopEducationId) o;

        if (!sopRef.equals(that.sopRef)) return false;
        return educationRef.equals(that.educationRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sopRef, educationRef);
    }

    @Override
    public String toString() {
        return "SopEducationId{" +
                "sopRef=" + sopRef +
                ", educationRef=" + educationRef +
                '}';
    }
}
