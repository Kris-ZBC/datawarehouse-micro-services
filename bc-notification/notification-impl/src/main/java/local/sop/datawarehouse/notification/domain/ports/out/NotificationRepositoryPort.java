package local.sop.datawarehouse.notification.domain.ports.out;

import java.util.Optional;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.datawarehouse.notification.domain.model.Notification;
import local.sop.datawarehouse.notification.domain.model.valueobjects.NotificationId;

public interface NotificationRepositoryPort {
	Notification save(Notification notification);
	Optional<Notification> findById(UUID id);
	void makeNotificationSeen(UUID id);
	void deleteById(UUID id);
	Boolean compensate(NotificationId id, SagaOutcome sagaOutcome);
}
