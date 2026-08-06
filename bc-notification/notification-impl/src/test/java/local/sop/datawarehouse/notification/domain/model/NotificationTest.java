package local.sop.datawarehouse.notification.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.datawarehouse.notification.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.datawarehouse.notification.domain.model.valueobjects.MessageRef;
import local.sop.datawarehouse.notification.domain.model.valueobjects.NotificationId;

public class NotificationTest {
    @Test
    void shouldReturnNewInstance_whenMessageRefIsChanged() {
        UUID messageRef = UUID.randomUUID();
        UUID newMessageRef = UUID.randomUUID();

        Notification original = Notification.builder()
            .id(NotificationId.newId())
            .messageRef(MessageRef.of(messageRef))
            .seen(false)
            .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
            .build();

        Notification updated = original.withMessageRef(MessageRef.of(newMessageRef));

        assertNotSame(original, updated);
        assertEquals(newMessageRef, updated.getMessageRef().value());
        assertEquals(messageRef, original.getMessageRef().value());
    }

    @Test
    void shouldReturnNewInstance_whenSeenIsChanged() {
        UUID messageRef = UUID.randomUUID();

        Notification original = Notification.builder()
            .id(NotificationId.newId())
            .messageRef(MessageRef.of(messageRef))
            .seen(false)
            .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
            .build();

        Notification updated = original.withSeen(true);

        assertNotSame(original, updated);
        assertEquals(true, updated.getSeen());
        assertEquals(false, original.getSeen());
    }
}