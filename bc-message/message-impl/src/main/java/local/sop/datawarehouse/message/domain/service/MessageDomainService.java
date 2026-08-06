package local.sop.datawarehouse.message.domain.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import local.sop.datawarehouse.message.domain.model.Message;
import local.sop.datawarehouse.message.domain.model.valueobjects.MessageId;

@Service
public class MessageDomainService implements MessageDomain {

    @Override
    public Message create(UUID senderPersonRef, String message) {
        return new Message(
            MessageId.newId(),
            OffsetDateTime.now(),
            message,
            senderPersonRef
        );
    }
}