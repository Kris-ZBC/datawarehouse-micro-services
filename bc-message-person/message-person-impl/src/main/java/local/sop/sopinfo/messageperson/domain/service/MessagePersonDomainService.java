package local.sop.sopinfo.messageperson.domain.service;

import java.time.LocalDateTime;

import local.sop.sopinfo.messageperson.domain.model.MessagePerson;
import local.sop.sopinfo.messageperson.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

public class MessagePersonDomainService implements MessagePersonDomain{

    @Override
    public MessagePerson createMessagePerson(CompositeKey id, Boolean active) {
        /* Create a new MessagePerson instance with the builder pattern */
        return new  MessagePerson.Builder()
            .id(id)
            .active(active)
            .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
            .build();
    }

    @Override
    public MessagePerson toggleActivateMessagePerson(CompositeKey id, Boolean previousActive, LocalDateTime createdAt) {
        /* Implementation for toggling activation status */
        return new MessagePerson.Builder()
            .id(id)
            .active(!previousActive)
            .createdAt(new CreatedAtTimestamp(createdAt))
            .build();
    }

    @Override
    public MessagePerson deleteMessagePerson(CompositeKey id) {
        /* Implementation for deleting a MessagePerson, which could be repreented by setting active to false */
        return new MessagePerson.Builder()
            .id(id)
            .active(false)
            .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
            .build();
    }

}
