package local.sop.datawarehouse.message.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import local.sop.datawarehouse.message.domain.model.Message;

class MessageDomainServiceTest {

    private final MessageDomainService service = new MessageDomainService();

    @Test
    @DisplayName("should create message with generated id and current timestamp")
    void shouldCreateMessageWithGeneratedIdAndCurrentTimestamp() {
        UUID sender = UUID.randomUUID();

        OffsetDateTime before = OffsetDateTime.now().minusSeconds(1);
        Message result = service.create(sender, "Hello");
        OffsetDateTime after = OffsetDateTime.now().plusSeconds(1);

        assertNotNull(result.id());
        assertNotNull(result.id().value());
        assertNotNull(result.dateTimeSent());
        assertFalse(result.dateTimeSent().isBefore(before));
        assertFalse(result.dateTimeSent().isAfter(after));
        assertEquals("Hello", result.message());
        assertEquals(sender, result.senderPersonRef());
    }
}