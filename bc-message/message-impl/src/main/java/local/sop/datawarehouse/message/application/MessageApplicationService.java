package local.sop.datawarehouse.message.application;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.message.application.api.MessageDirectory;
import local.sop.datawarehouse.message.application.api.dto.CreateMessageCmd;
import local.sop.datawarehouse.message.application.api.dto.MessageResponse;
import local.sop.datawarehouse.message.application.mapper.MessageResponseMapper;
import local.sop.datawarehouse.message.domain.model.Message;
import local.sop.datawarehouse.message.domain.model.valueobjects.MessageId;
import local.sop.datawarehouse.message.domain.ports.out.MessageRepositoryPort;
import local.sop.datawarehouse.message.domain.service.MessageDomain;

@Service
public class MessageApplicationService implements MessageDirectory {

    private final MessageDomain messageDomain;
    private final MessageRepositoryPort messageRepositoryPort;
    private final MessageResponseMapper messageResponseMapper;
	private static final Logger log = LoggerFactory.getLogger(MessageApplicationService.class);

    public MessageApplicationService(
        MessageDomain messageDomain,
        MessageRepositoryPort messageRepositoryPort,
        MessageResponseMapper messageResponseMapper
    ) {
        this.messageDomain = messageDomain;
        this.messageRepositoryPort = messageRepositoryPort;
        this.messageResponseMapper = messageResponseMapper;
    }

    @Override
    @Transactional
    public MessageResponse create(CreateMessageCmd cmd) {
        if (cmd == null) {
            throw new ValidationException("message.create.required", Map.of("function", "createForPersons"));
        }

        Message saved = messageRepositoryPort.save(
            messageDomain.create(
                cmd.senderPersonRef(),
                cmd.message()
            )
        );

        return messageResponseMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MessageResponse> findById(UUID id) {
        if (id == null) {
            throw new ValidationException("message.id.required", Map.of("field", "id"));
        }

        Message message = messageRepositoryPort.findById(MessageId.of(id))
            .orElseThrow(() -> new ValidationException(
                "message.not.found",
                Map.of("field", "id", "id", id)
            ));

        return Optional.of(messageResponseMapper.toResponse(message));
    }

	@Override
	public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
		log.info("Compensate called from class {}", clazz.getSimpleName());
		var found = messageRepositoryPort.findById(MessageId.of(id));
		if (found.isEmpty()) {
			return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
		}
		boolean result = messageRepositoryPort.compensate(MessageId.of(id), sagaState);
		if (result) {
			return new ResponseCompensated(SagaOutcome.COMPENSATE, true);
		}
		return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
	}
}