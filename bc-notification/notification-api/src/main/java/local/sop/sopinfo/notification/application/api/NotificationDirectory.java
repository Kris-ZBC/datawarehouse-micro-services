package local.sop.sopinfo.notification.application.api;

import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.notification.application.api.dto.CreateNotificationCmd;
import local.sop.sopinfo.notification.application.api.dto.NotificationResponse;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;

public interface NotificationDirectory {
	UUID createNotification(CreateNotificationCmd cmd);
	Optional<NotificationResponse> findById(UUID id);
	void makeNotificationSeen(UUID id);
	void deleteNotification(UUID id);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
