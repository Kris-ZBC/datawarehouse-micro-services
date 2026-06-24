package local.sop.sopinfo.educationinstructor.interfaceadapters.persistence.jpa;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class EducationInstructorId implements Serializable {
    @Column(name = "education_ref", nullable = false)
    private UUID educationRef;
    @Column(name = "instructor_ref", nullable = false)
    private UUID instructorRef;

    protected EducationInstructorId() {
        // Default constructor for JPA
    }

    public EducationInstructorId(UUID educationRef, UUID instructorRef) {
        this.educationRef = educationRef;
        this.instructorRef = instructorRef;
    }

    public UUID getEducationRef() {
        return educationRef;
    }

    public UUID getInstructorRef() {
        return instructorRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EducationInstructorId that = (EducationInstructorId) o;
        return Objects.equals(educationRef, that.educationRef) &&
               Objects.equals(instructorRef, that.instructorRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(educationRef, instructorRef);
    }

    @Override
    public String toString() {
        return "EducationInstructorId{" +
               "educationRef=" + educationRef +
               ", instructorRef=" + instructorRef +
               '}';
    }
}
