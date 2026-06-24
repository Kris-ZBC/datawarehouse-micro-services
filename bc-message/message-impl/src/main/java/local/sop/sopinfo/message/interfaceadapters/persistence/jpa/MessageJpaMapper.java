package local.sop.sopinfo.message.interfaceadapters.persistence.jpa;

import org.springframework.stereotype.Component;

import local.sop.sopinfo.message.domain.model.Message;
import local.sop.sopinfo.message.domain.model.valueobjects.MessageId;

@Component
public class MessageJpaMapper {

    public MessageEntity toEntity(Message message) {
        return new MessageEntity()
            .withId(message.id().value())
            .withDateTimeSent(message.dateTimeSent())
            .withMessage(message.message())
            .withSenderPersonRef(message.senderPersonRef());
    }

    public Message toDomain(MessageEntity entity) {
        return new Message(
            MessageId.of(entity.getId()),
            entity.getDateTimeSent(),
            entity.getMessage(),
            entity.getSenderPersonRef()
        );
    }
}