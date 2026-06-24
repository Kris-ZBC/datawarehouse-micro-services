package local.sop.sopinfo.message.saga.application.ports.out.message;

import java.util.UUID;

import local.sop.sopinfo.message.saga.application.api.dto.MessageResponse;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;

public interface MessagePort {
	MessageResponse create(UUID senderPersonRef, String message);
	MessageResponse findById(UUID id);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
