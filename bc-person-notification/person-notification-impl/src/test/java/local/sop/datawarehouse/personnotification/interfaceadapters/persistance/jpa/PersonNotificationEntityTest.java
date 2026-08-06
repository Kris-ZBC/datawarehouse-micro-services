package local.sop.datawarehouse.personnotification.interfaceadapters.persistance.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.personnotification.interfaceadapters.persistence.jpa.PersonNotificationEntity;
import local.sop.datawarehouse.personnotification.interfaceadapters.persistence.jpa.PersonNotificationId;

class PersonNotificationEntityTest {

    @Test
    void builder_createsEntity_andGettersWork() {
        UUID notificationRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        PersonNotificationId id = new PersonNotificationId(personRef, notificationRef);

        PersonNotificationEntity entity = PersonNotificationEntity.builder()
                .id(id)
                .active(true)
                .build();

        assertEquals(notificationRef, entity.getId().getNotificationRef());
        assertEquals(personRef, entity.getId().getPersonRef());
        assertTrue(entity.isActive());
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    void withActive_updatesField() {
        PersonNotificationId id = new PersonNotificationId(UUID.randomUUID(), UUID.randomUUID());

        PersonNotificationEntity entity = PersonNotificationEntity.builder()
                .id(id)
                .active(true)
                .build();

        entity.withActive(false);

        assertFalse(entity.isActive());
    }

    @Test
    void builder_throws_whenIdMissing() {
        var ex =assertThrows(ValidationException.class, () ->
                PersonNotificationEntity.builder()
                        .active(true)
                        .build()
        );
        assertEquals("key.required", ex.getMessage());
    }

    @Test
    void builder_throws_whenActiveMissing() {
        PersonNotificationId id = new PersonNotificationId(UUID.randomUUID(), UUID.randomUUID());

        var ex = assertThrows(ValidationException.class, () ->
                PersonNotificationEntity.builder()
                        .id(id)
                        .build()
        );
        assertEquals("personnotification.active.required", ex.getMessage());
    }
}