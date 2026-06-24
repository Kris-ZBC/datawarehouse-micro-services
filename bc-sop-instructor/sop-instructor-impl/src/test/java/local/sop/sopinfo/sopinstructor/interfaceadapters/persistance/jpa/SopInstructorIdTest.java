package local.sop.sopinfo.sopinstructor.interfaceadapters.persistance.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.sopinstructor.interfaceadapters.persistence.jpa.SopInstructorId;

public class SopInstructorIdTest {

    // Test the getters
    @Test
    void getters_returnCorrectValues() {
        UUID sopRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();
        
        SopInstructorId id = new SopInstructorId(sopRef, instructorRef);
        
        assertEquals(sopRef, id.getSopRef());
        assertEquals(instructorRef, id.getInstructorRef());
    }

    // Test the equals and hashCode methods
    @Test
    void equals_shouldReturnTrue_whenSameValues() {
        UUID sopRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();
        
        SopInstructorId id1 = new SopInstructorId(sopRef, instructorRef);
        SopInstructorId id2 = new SopInstructorId(sopRef, instructorRef);
        
        assertEquals(id1, id2);
        // Since they are equal, their hash codes should also be equal
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    // Test that equals returns false when sopRefs are different
    @Test
    void equals_shouldReturnFalse_whenDifferentsopRefs() {
        UUID instructorRef = UUID.randomUUID();
        
        SopInstructorId id1 = new SopInstructorId(UUID.randomUUID(), instructorRef);
        SopInstructorId id2 = new SopInstructorId(UUID.randomUUID(), instructorRef);
        
        assertNotEquals(id1, id2);
    }

    // Test that equals returns false when instructorRefs are different
    @Test
    void equals_shouldReturnFalse_whenDifferentinstructorRefs() {
        UUID sopRef = UUID.randomUUID();
        
        SopInstructorId id1 = new SopInstructorId(sopRef, UUID.randomUUID());
        SopInstructorId id2 = new SopInstructorId(sopRef, UUID.randomUUID());
        
        assertNotEquals(id1, id2);
    }

    // Test that equals returns false when compared with null
    @Test
    void equals_shouldReturnFalse_whenComparedWithNull() {
        UUID sopRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();
        
        SopInstructorId id = new SopInstructorId(sopRef, instructorRef);
        
        assertNotEquals(id, null);
    }

    // Test that equals returns true when comparing the same object
    @Test
    void equals_shouldReturnTrue_whenSameObject() {
        UUID sopRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();
        
        SopInstructorId id = new SopInstructorId(sopRef, instructorRef);
        
        assertEquals(id, id);
    }

    // Test that equals returns false when compared with an object of a different class
    @Test
    void equals_shouldReturnFalse_whenDifferentClass() {
        UUID sopRef = UUID.randomUUID();
        UUID instructorRef = UUID.randomUUID();
        
        SopInstructorId id = new SopInstructorId(sopRef, instructorRef);
        
        assertNotEquals(id, "some string");
    }

}
