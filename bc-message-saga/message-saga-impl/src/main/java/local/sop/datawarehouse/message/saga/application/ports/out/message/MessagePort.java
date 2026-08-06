package local.sop.datawarehouse.message.saga.application.ports.out.message;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.message.saga.application.api.dto.MessageResponse;

public interface MessagePort {
	MessageResponse create(UUID senderPersonRef, String message);
	MessageResponse findById(UUID id);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
