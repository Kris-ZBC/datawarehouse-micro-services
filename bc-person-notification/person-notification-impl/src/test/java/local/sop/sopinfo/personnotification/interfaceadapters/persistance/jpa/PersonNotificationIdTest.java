package local.sop.sopinfo.personnotification.interfaceadapters.persistance.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.datawarehouse.personnotification.interfaceadapters.persistence.jpa.PersonNotificationId;

public class PersonNotificationIdTest {

    // Test the getters
    @Test
    void getters_returnCorrectValues() {
        UUID notificationRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();
        
        PersonNotificationId id = new PersonNotificationId(personRef, notificationRef);
        
        assertEquals(personRef, id.getPersonRef());
        assertEquals(notificationRef, id.getNotificationRef());
    }

    // Test the equals and hashCode methods
    @Test
    void equals_shouldReturnTrue_whenSameValues() {
        UUID personRef = UUID.randomUUID();
        UUID notificationRef = UUID.randomUUID();
        
        PersonNotificationId id1 = new PersonNotificationId(personRef, notificationRef);
        PersonNotificationId id2 = new PersonNotificationId(personRef, notificationRef);
        
        assertEquals(id1, id2);
        // Since they are equal, their hash codes should also be equal
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    // Test that equals returns false when notificationRefs are different
    @Test
    void equals_shouldReturnFalse_whenDifferentpersonRefs() {
        UUID notificationRef = UUID.randomUUID();
        
        PersonNotificationId id1 = new PersonNotificationId(UUID.randomUUID(), notificationRef);
        PersonNotificationId id2 = new PersonNotificationId(UUID.randomUUID(), notificationRef);
        
        assertNotEquals(id1, id2);
    }

    // Test that equals returns false when personRefs are different
    @Test
    void equals_shouldReturnFalse_whenDifferentnotificationRefs() {
        UUID personRef = UUID.randomUUID();
        
        PersonNotificationId id1 = new PersonNotificationId(personRef, UUID.randomUUID());
        PersonNotificationId id2 = new PersonNotificationId(personRef, UUID.randomUUID());
        
        assertNotEquals(id1, id2);
    }

    // Test that equals returns false when compared with null
    @Test
    void equals_shouldReturnFalse_whenComparedWithNull() {
        UUID personRef = UUID.randomUUID();
        UUID notificationRef = UUID.randomUUID();
        
        PersonNotificationId id = new PersonNotificationId(personRef, notificationRef);
        
        assertNotEquals(id, null);
    }

    // Test that equals returns true when comparing the same object
    @Test
    void equals_shouldReturnTrue_whenSameObject() {
        UUID personRef = UUID.randomUUID();
        UUID notificationRef = UUID.randomUUID();
        
        PersonNotificationId id = new PersonNotificationId(personRef, notificationRef);
        
        assertEquals(id, id);
    }

    // Test that equals returns false when compared with an object of a different class
    @Test
    void equals_shouldReturnFalse_whenDifferentClass() {
        UUID personRef = UUID.randomUUID();
        UUID notificationRef = UUID.randomUUID();
        
        PersonNotificationId id = new PersonNotificationId(personRef, notificationRef);
        
        assertNotEquals(id, "some string");
    }

}
