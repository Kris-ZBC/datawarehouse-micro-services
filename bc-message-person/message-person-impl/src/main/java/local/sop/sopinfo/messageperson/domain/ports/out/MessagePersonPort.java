package local.sop.sopinfo.messageperson.domain.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.messageperson.domain.model.MessagePerson;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;

public interface MessagePersonPort {
    Optional<MessagePerson> findById(CompositeKey id);
    MessagePerson save(MessagePerson messagePerson);
    void update(MessagePerson messagePerson);
    List<MessagePerson> findByMessageRef(UUID messageRef);
    List<MessagePerson> findByPersonRef(UUID personRef);
}
