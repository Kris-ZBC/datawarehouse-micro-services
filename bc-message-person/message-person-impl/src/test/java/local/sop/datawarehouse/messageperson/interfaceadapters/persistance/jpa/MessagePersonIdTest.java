package local.sop.datawarehouse.messageperson.interfaceadapters.persistance.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.datawarehouse.messageperson.interfaceadapters.persistence.jpa.MessagePersonId;

public class MessagePersonIdTest {

    // Test the getters
    @Test
    void getters_returnCorrectValues() {
        UUID messageRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();
        
        MessagePersonId id = new MessagePersonId(messageRef, personRef);
        
        assertEquals(messageRef, id.getMessageRef());
        assertEquals(personRef, id.getPersonRef());
    }

    // Test the equals and hashCode methods
    @Test
    void equals_shouldReturnTrue_whenSameValues() {
        UUID messageRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();
        
        MessagePersonId id1 = new MessagePersonId(messageRef, personRef);
        MessagePersonId id2 = new MessagePersonId(messageRef, personRef);
        
        assertEquals(id1, id2);
        // Since they are equal, their hash codes should also be equal
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    // Test that equals returns false when messageRefs are different
    @Test
    void equals_shouldReturnFalse_whenDifferentmessageRefs() {
        UUID personRef = UUID.randomUUID();
        
        MessagePersonId id1 = new MessagePersonId(UUID.randomUUID(), personRef);
        MessagePersonId id2 = new MessagePersonId(UUID.randomUUID(), personRef);
        
        assertNotEquals(id1, id2);
    }

    // Test that equals returns false when personRefs are different
    @Test
    void equals_shouldReturnFalse_whenDifferentpersonRefs() {
        UUID messageRef = UUID.randomUUID();
        
        MessagePersonId id1 = new MessagePersonId(messageRef, UUID.randomUUID());
        MessagePersonId id2 = new MessagePersonId(messageRef, UUID.randomUUID());
        
        assertNotEquals(id1, id2);
    }

    // Test that equals returns false when compared with null
    @Test
    void equals_shouldReturnFalse_whenComparedWithNull() {
        UUID messageRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();
        
        MessagePersonId id = new MessagePersonId(messageRef, personRef);
        
        assertNotEquals(id, null);
    }

    // Test that equals returns true when comparing the same object
    @Test
    void equals_shouldReturnTrue_whenSameObject() {
        UUID messageRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();
        
        MessagePersonId id = new MessagePersonId(messageRef, personRef);
        
        assertEquals(id, id);
    }

    // Test that equals returns false when compared with an object of a different class
    @Test
    void equals_shouldReturnFalse_whenDifferentClass() {
        UUID messageRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();
        
        MessagePersonId id = new MessagePersonId(messageRef, personRef);
        
        assertNotEquals(id, "some string");
    }

}
