package local.sop.sopinfo.educationinstructor.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class EducationInstructorIdTest {

    // Test the getters
    @Test
    void getters_returnCorrectValues() {
        UUID educationRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();
        
        EducationInstructorId id = new EducationInstructorId(educationRef, instructorRef);
        
        assertEquals(educationRef, id.getEducationRef());
        assertEquals(instructorRef, id.getInstructorRef());
    }

    // Test the equals and hashCode methods
    @Test
    void equals_shouldReturnTrue_whenSameValues() {
        UUID educationRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();
        
        EducationInstructorId id1 = new EducationInstructorId(educationRef, instructorRef);
        EducationInstructorId id2 = new EducationInstructorId(educationRef, instructorRef);
        
        assertEquals(id1, id2);
        // Since they are equal, their hash codes should also be equal
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    // Test that equals returns false when educationRefs are different
    @Test
    void equals_shouldReturnFalse_whenDifferenteducationRefs() {
        UUID instructorRef = UUID.randomUUID();
        
        EducationInstructorId id1 = new EducationInstructorId(UUID.randomUUID(), instructorRef);
        EducationInstructorId id2 = new EducationInstructorId(UUID.randomUUID(), instructorRef);
        
        assertNotEquals(id1, id2);
    }

    // Test that equals returns false when instructorRefs are different
    @Test
    void equals_shouldReturnFalse_whenDifferentinstructorRefs() {
        UUID educationRef = UUID.randomUUID();
        
        EducationInstructorId id1 = new EducationInstructorId(educationRef, UUID.randomUUID());
        EducationInstructorId id2 = new EducationInstructorId(educationRef, UUID.randomUUID());
        
        assertNotEquals(id1, id2);
    }

    // Test that equals returns false when compared with null
    @Test
    void equals_shouldReturnFalse_whenComparedWithNull() {
        UUID educationRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();
        
        EducationInstructorId id = new EducationInstructorId(educationRef, instructorRef);
        
        assertNotEquals(id, null);
    }

    // Test that equals returns true when comparing the same object
    @Test
    void equals_shouldReturnTrue_whenSameObject() {
        UUID educationRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();
        
        EducationInstructorId id = new EducationInstructorId(educationRef, instructorRef);
        
        assertEquals(id, id);
    }

    // Test that equals returns false when compared with an object of a different class
    @Test
    void equals_shouldReturnFalse_whenDifferentClass() {
        UUID educationRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();
        
        EducationInstructorId id = new EducationInstructorId(educationRef, instructorRef);
        
        assertNotEquals(id, "some string");
    }

}

