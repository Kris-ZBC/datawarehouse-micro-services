package local.sop.datawarehouse.message.domain.ports.out;

import java.util.Optional;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.datawarehouse.message.domain.model.Message;
import local.sop.datawarehouse.message.domain.model.valueobjects.MessageId;

public interface MessageRepositoryPort {

    Message save(Message message);

	Optional<Message> findById(MessageId id);
	Boolean compensate(MessageId id, SagaOutcome sagaState);
}