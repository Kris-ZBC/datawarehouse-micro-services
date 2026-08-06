package local.sop.datawarehouse.message.application.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import local.sop.datawarehouse.message.application.api.dto.MessageResponse;
import local.sop.datawarehouse.message.domain.model.Message;
import local.sop.datawarehouse.message.domain.model.valueobjects.MessageId;

class MessageResponseMapperTest {

    private final MessageResponseMapper mapper = new MessageResponseMapper();

    @Test
    @DisplayName("should map domain message to response")
    void shouldMapDomainMessageToResponse() {
        UUID id = UUID.randomUUID();
        OffsetDateTime sentAt = OffsetDateTime.now();
        UUID sender = UUID.randomUUID();

        Message message = new Message(
            MessageId.of(id),
            sentAt,
            "Hello",
            sender
        );

        MessageResponse response = mapper.toResponse(message);

        assertEquals(id, response.id());
        assertEquals(sentAt, response.dateTimeSent());
        assertEquals("Hello", response.message());
        assertEquals(sender, response.senderPersonRef());
    }
}