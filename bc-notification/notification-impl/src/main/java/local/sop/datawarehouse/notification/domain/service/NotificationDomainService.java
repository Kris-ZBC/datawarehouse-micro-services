package local.sop.datawarehouse.notification.domain.service;

import java.util.UUID;

import local.sop.datawarehouse.notification.domain.model.Notification;
import local.sop.datawarehouse.notification.domain.model.valueobjects.MessageRef;

public class NotificationDomainService implements NotificationDomain {

	@Override
	public Notification createNotification(UUID messageRef) {
		return Notification.builder()
			.messageRef(MessageRef.of(messageRef))
			.build();
	}

}
