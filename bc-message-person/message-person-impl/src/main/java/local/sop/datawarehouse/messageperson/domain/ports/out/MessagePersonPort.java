package local.sop.datawarehouse.messageperson.domain.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.datawarehouse.messageperson.domain.model.MessagePerson;

public interface MessagePersonPort {
    Optional<MessagePerson> findById(CompositeKey id);
    MessagePerson save(MessagePerson messagePerson);
    void update(MessagePerson messagePerson);
    List<MessagePerson> findByMessageRef(UUID messageRef);
    List<MessagePerson> findByPersonRef(UUID personRef);
}
