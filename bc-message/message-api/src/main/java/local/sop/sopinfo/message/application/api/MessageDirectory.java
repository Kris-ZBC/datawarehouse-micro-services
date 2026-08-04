package local.sop.sopinfo.message.application.api;

import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.message.application.api.dto.CreateMessageCmd;
import local.sop.sopinfo.message.application.api.dto.MessageResponse;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;

public interface MessageDirectory {
    MessageResponse create(CreateMessageCmd cmd);
	Optional<MessageResponse> findById(UUID id);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}