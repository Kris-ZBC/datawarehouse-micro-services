package local.sop.sopinfo.message.interfaceadapters.persistence.jpa;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import local.sop.sopinfo.message.domain.model.Message;
import local.sop.sopinfo.message.domain.model.valueobjects.MessageId;
import local.sop.sopinfo.message.domain.ports.out.MessageRepositoryPort;
import local.sop.sopinfo.sharedkernel.exceptions.ConflictException;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

@Component
public class MessageRepositoryJpaAdapter implements MessageRepositoryPort {

    private final MessageSpringDataRepository repository;
    private final MessageJpaMapper mapper;

    public MessageRepositoryJpaAdapter(
        MessageSpringDataRepository repository,
        MessageJpaMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Message save(Message message) {
        return mapper.toDomain(repository.save(mapper.toEntity(message)));
    }

	@Override
    public Optional<Message> findById(MessageId id) {
        return repository.findById(id.value())
            .map(mapper::toDomain);
    }

	@Override
	public Boolean compensate(MessageId id, SagaOutcome sagaState) {
		if(sagaState != SagaOutcome.COMPENSATE) {
			throw new ConflictException("compensate.wrong_state", Map.of("compensate", sagaState.name()));
		}
		var found = findById(id);
		if (found.isEmpty()) {
			return false;
		}
		return repository.delete(id.value()) == 1;
	}
}