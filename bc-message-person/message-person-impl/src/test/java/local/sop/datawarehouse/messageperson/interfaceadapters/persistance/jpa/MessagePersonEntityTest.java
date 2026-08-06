package local.sop.datawarehouse.messageperson.interfaceadapters.persistance.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.messageperson.interfaceadapters.persistence.jpa.MessagePersonEntity;
import local.sop.datawarehouse.messageperson.interfaceadapters.persistence.jpa.MessagePersonId;

class MessagePersonEntityTest {

    @Test
    void builder_createsEntity_andGettersWork() {
        UUID messageRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        MessagePersonId id = new MessagePersonId(messageRef, personRef);

        MessagePersonEntity entity = MessagePersonEntity.builder()
                .id(id)
                .active(true)
                .build();

        assertEquals(messageRef, entity.getId().getMessageRef());
        assertEquals(personRef, entity.getId().getPersonRef());
        assertTrue(entity.isActive());
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    void withActive_updatesField() {
        MessagePersonId id = new MessagePersonId(UUID.randomUUID(), UUID.randomUUID());

        MessagePersonEntity entity = MessagePersonEntity.builder()
                .id(id)
                .active(true)
                .build();

        entity.withActive(false);

        assertFalse(entity.isActive());
    }

    @Test
    void builder_throws_whenIdMissing() {
        var ex =assertThrows(ValidationException.class, () ->
                MessagePersonEntity.builder()
                        .active(true)
                        .build()
        );
        assertEquals("key.required", ex.getMessage());
    }

    @Test
    void builder_throws_whenActiveMissing() {
        MessagePersonId id = new MessagePersonId(UUID.randomUUID(), UUID.randomUUID());

        var ex = assertThrows(ValidationException.class, () ->
                MessagePersonEntity.builder()
                        .id(id)
                        .build()
        );
        assertEquals("message-person.active.required", ex.getMessage());
    }
}