package local.sop.datawarehouse.messageperson.interfaceadapters.persistence.jpa;

import org.springframework.stereotype.Component;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.datawarehouse.messageperson.domain.model.MessagePerson;
import local.sop.datawarehouse.messageperson.domain.model.valueobjects.CreatedAtTimestamp;

@Component
public class MessagePersonDomainJpaMapper {
    /** JPA -> Domain */
    public MessagePerson toDomain(MessagePersonEntity e) {
        return new MessagePerson.Builder()
            .id(new CompositeKey(e.getId().getMessageRef(), e.getId().getPersonRef()))
            .active(e.isActive())
            .createdAt(new CreatedAtTimestamp(e.getCreatedAt()))
            .build();
    }

    /** Domain -> JPA */

    /* preconditions
    * MessagePerson must have a valid Compositekey id with non-null key1 and key2
    * Key1 equals messageRef and Key2 equals personRef in the MessagePersonEntity */
    public MessagePersonEntity toEntity(MessagePerson s) {
        return MessagePersonEntity.builder()
            .id(new MessagePersonId(s.getId().key1(), s.getId().key2()))
            .active(s.isActive())
            .build();

    }

    /* Domain -> existing JPA entity */
    public void updateIntoEntity(MessagePerson s, MessagePersonEntity me) {
        me.withActive(s.isActive());

    }

}
