package local.sop.sopinfo.message.saga.application.ports.out.notification;

import java.util.UUID;

import local.sop.sopinfo.message.saga.application.api.dto.NotificationResponse;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;

public interface NotificationPort {
	UUID create(UUID messageRef);
	NotificationResponse findById(UUID id);
	void deleteNotification(UUID id);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
