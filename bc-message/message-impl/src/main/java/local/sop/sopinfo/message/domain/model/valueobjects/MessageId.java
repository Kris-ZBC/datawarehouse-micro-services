package local.sop.sopinfo.message.domain.model.valueobjects;

import java.util.Map;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;

public record MessageId(UUID value) implements DomainId {

    public MessageId {
        if (value == null) {
            throw new ValidationException("key.required", Map.of("field", "id"));
        }
    }

    public static MessageId newId() {
        return new MessageId(UUID.randomUUID());
    }

    public static MessageId of(UUID value) {
        return new MessageId(value);
    }
}