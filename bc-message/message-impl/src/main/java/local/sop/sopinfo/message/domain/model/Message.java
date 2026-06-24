package local.sop.sopinfo.message.domain.model;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import local.sop.sopinfo.message.domain.model.valueobjects.MessageId;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public final class Message {

    private final MessageId id;
    private final OffsetDateTime dateTimeSent;
    private final String message;
    private final UUID senderPersonRef;

    public Message(
        MessageId id,
        OffsetDateTime dateTimeSent,
        String message,
        UUID senderPersonRef
    ) {
        if (id == null) {
            throw new ValidationException("message.id.required", Map.of("field", "id"));
        }

        if (dateTimeSent == null) {
            throw new ValidationException("message.dateTimeSent.required", Map.of("field", "dateTimeSent"));
        }

        if (senderPersonRef == null) {
            throw new ValidationException("message.senderPersonRef.required", Map.of("field", "senderPersonRef"));
        }

        if (message == null || message.trim().isBlank()) {
            throw new ValidationException("message.message.required", Map.of("field", "message"));
        }

        String normalizedMessage = message.trim();
        if (normalizedMessage.length() > 4000) {
            throw new ValidationException("message.message.maxLength", Map.of("field", "message", "max", 4000));
        }

        this.id = id;
        this.dateTimeSent = dateTimeSent;
        this.message = normalizedMessage;
        this.senderPersonRef = senderPersonRef;
    }

    public MessageId id() {
        return id;
    }

    public OffsetDateTime dateTimeSent() {
        return dateTimeSent;
    }

    public String message() {
        return message;
    }

    public UUID senderPersonRef() {
        return senderPersonRef;
    }
}