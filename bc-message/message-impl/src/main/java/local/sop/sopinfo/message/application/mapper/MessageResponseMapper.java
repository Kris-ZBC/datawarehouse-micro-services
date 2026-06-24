package local.sop.sopinfo.message.application.mapper;

import org.springframework.stereotype.Component;

import local.sop.sopinfo.message.application.api.dto.MessageResponse;
import local.sop.sopinfo.message.domain.model.Message;

@Component
public class MessageResponseMapper {

    public MessageResponse toResponse(Message message) {
        return new MessageResponse(
            message.id().value(),
            message.dateTimeSent(),
            message.message(),
            message.senderPersonRef()
        );
    }
}