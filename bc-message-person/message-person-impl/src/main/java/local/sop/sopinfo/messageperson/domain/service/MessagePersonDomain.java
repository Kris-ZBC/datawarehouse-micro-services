package local.sop.sopinfo.messageperson.domain.service;

import java.time.LocalDateTime;

import local.sop.sopinfo.messageperson.domain.model.MessagePerson;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;

public interface MessagePersonDomain {
    public MessagePerson createMessagePerson(CompositeKey id, Boolean active);
    public MessagePerson toggleActivateMessagePerson(CompositeKey id, Boolean previousActive, LocalDateTime createdAt);
    public MessagePerson deleteMessagePerson(CompositeKey id);
}
