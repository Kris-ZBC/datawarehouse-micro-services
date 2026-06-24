package local.sop.sopinfo.sopinstructor.interfaceadapters.persistence.jpa;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class SopInstructorId implements Serializable {
    @Column(name = "sop_ref", nullable = false)
    private UUID sopRef;

    @Column(name = "instructor_ref", nullable = false)
    private UUID instructorRef;

    public SopInstructorId(UUID sopRef, UUID instructorRef) {
        this.sopRef = sopRef;
        this.instructorRef = instructorRef;
    }
    
    protected SopInstructorId() {} // JPA requires a default constructor

    public UUID getSopRef() {
        return sopRef;
    }

    public UUID getInstructorRef() {
        return instructorRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SopInstructorId that = (SopInstructorId) o;

        if (!sopRef.equals(that.sopRef)) return false;
        return instructorRef.equals(that.instructorRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sopRef, instructorRef);
    }
}
