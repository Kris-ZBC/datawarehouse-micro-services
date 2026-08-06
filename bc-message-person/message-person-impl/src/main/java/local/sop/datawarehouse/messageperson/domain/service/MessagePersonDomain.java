package local.sop.datawarehouse.messageperson.domain.service;

import java.time.LocalDateTime;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.datawarehouse.messageperson.domain.model.MessagePerson;

public interface MessagePersonDomain {
    public MessagePerson createMessagePerson(CompositeKey id, Boolean active);
    public MessagePerson toggleActivateMessagePerson(CompositeKey id, Boolean previousActive, LocalDateTime createdAt);
    public MessagePerson deleteMessagePerson(CompositeKey id);
}
