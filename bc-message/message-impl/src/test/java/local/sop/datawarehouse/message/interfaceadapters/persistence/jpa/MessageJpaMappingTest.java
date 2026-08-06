package local.sop.datawarehouse.message.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import local.sop.datawarehouse.message.domain.model.Message;
import local.sop.datawarehouse.message.domain.model.valueobjects.MessageId;

class MessageJpaMappingTest {

    private final MessageJpaMapper mapper = new MessageJpaMapper();

    @Test
    @DisplayName("should map domain to entity")
    void shouldMapDomainToEntity() {
        UUID id = UUID.randomUUID();
        OffsetDateTime sentAt = OffsetDateTime.now();
        UUID sender = UUID.randomUUID();
 
        Message message = new Message(
            MessageId.of(id),
            sentAt,
            "Hello",
            sender
        );

        MessageEntity entity = mapper.toEntity(message);

        assertEquals(id, entity.getId());
        assertEquals(sentAt, entity.getDateTimeSent());
        assertEquals("Hello", entity.getMessage());
        assertEquals(sender, entity.getSenderPersonRef());
    }

    @Test
    @DisplayName("should map entity to domain")
    void shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        OffsetDateTime sentAt = OffsetDateTime.now();
        UUID sender = UUID.randomUUID();

        MessageEntity entity = new MessageEntity()
        	.withId(id)
        	.withDateTimeSent(sentAt)
        	.withMessage("Hello")
        	.withSenderPersonRef(sender);
		

        Message message = mapper.toDomain(entity);

        assertEquals(id, message.id().value());
        assertEquals(sentAt, message.dateTimeSent());
        assertEquals("Hello", message.message());
        assertEquals(sender, message.senderPersonRef());
    }
}