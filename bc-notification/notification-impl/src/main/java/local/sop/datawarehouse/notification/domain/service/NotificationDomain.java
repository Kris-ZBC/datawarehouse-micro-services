package local.sop.datawarehouse.notification.domain.service;

import java.util.UUID;

import local.sop.datawarehouse.notification.domain.model.Notification;

public interface NotificationDomain {
	Notification createNotification(UUID messageRef);
}
