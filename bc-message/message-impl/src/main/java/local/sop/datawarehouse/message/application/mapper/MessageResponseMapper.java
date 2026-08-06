package local.sop.datawarehouse.message.application.mapper;

import org.springframework.stereotype.Component;

import local.sop.datawarehouse.message.application.api.dto.MessageResponse;
import local.sop.datawarehouse.message.domain.model.Message;

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