package local.sop.datawarehouse.message.saga.application.ports.out.notification;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.message.saga.application.api.dto.NotificationResponse;

public interface NotificationPort {
	UUID create(UUID messageRef);
	NotificationResponse findById(UUID id);
	void deleteNotification(UUID id);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
