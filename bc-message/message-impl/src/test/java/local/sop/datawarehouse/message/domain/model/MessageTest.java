package local.sop.datawarehouse.message.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.message.domain.model.valueobjects.MessageId;

class MessageTest {

    @Test
    @DisplayName("should create message when input is valid")
    void shouldCreateMessageWhenInputIsValid() {
        MessageId id = MessageId.newId();
        OffsetDateTime sentAt = OffsetDateTime.now();
        UUID sender = UUID.randomUUID();


        Message message = new Message(
            id,
            sentAt,
            "  Hello world  ",
            sender
        );

        assertEquals(id, message.id());
        assertEquals(sentAt, message.dateTimeSent());
        assertEquals("Hello world", message.message());
        assertEquals(sender, message.senderPersonRef());
    }

    @Test
    @DisplayName("should throw when id is null")
    void shouldThrowWhenIdIsNull() {
        assertThrows(
            ValidationException.class,
            () -> new Message(
                null,
                OffsetDateTime.now(),
                "Hello",
                UUID.randomUUID()
            )
        );
    }

    @Test
    @DisplayName("should throw when dateTimeSent is null")
    void shouldThrowWhenDateTimeSentIsNull() {
        assertThrows(
            ValidationException.class,
            () -> new Message(
                MessageId.newId(),
                null,
                "Hello",
                UUID.randomUUID()
            )
        );
    }

    @Test
    @DisplayName("should throw when senderPersonRef is null")
    void shouldThrowWhenSenderPersonRefIsNull() {
        assertThrows(
            ValidationException.class,
            () -> new Message(
                MessageId.newId(),
                OffsetDateTime.now(),
                "Hello",
                null
            )
        );
    }

    @Test
    @DisplayName("should throw when message is null")
    void shouldThrowWhenMessageIsNull() {
        assertThrows(
            ValidationException.class,
            () -> new Message(
                MessageId.newId(),
                OffsetDateTime.now(),
                null,
                UUID.randomUUID()
            )
        );
    }

    @Test
    @DisplayName("should throw when message is blank")
    void shouldThrowWhenMessageIsBlank() {
        assertThrows(
            ValidationException.class,
            () -> new Message(
                MessageId.newId(),
                OffsetDateTime.now(),
                "   ",
                UUID.randomUUID()
            )
        );
    }

    @Test
    @DisplayName("should throw when message exceeds max length")
    void shouldThrowWhenMessageExceedsMaxLength() {
        String tooLong = "a".repeat(4001);

        assertThrows(
            ValidationException.class,
            () -> new Message(
                MessageId.newId(),
                OffsetDateTime.now(),
                tooLong,
                UUID.randomUUID()
            )
        );
    }
}