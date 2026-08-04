package local.sop.sopinfo.message.domain.ports.out;

import java.util.Optional;

import local.sop.sopinfo.message.domain.model.Message;
import local.sop.sopinfo.message.domain.model.valueobjects.MessageId;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

public interface MessageRepositoryPort {

    Message save(Message message);

	Optional<Message> findById(MessageId id);
	Boolean compensate(MessageId id, SagaOutcome sagaState);
}