package local.sop.sopinfo.notification.domain.service;

import java.util.UUID;

import local.sop.sopinfo.notification.domain.model.Notification;

public interface NotificationDomain {
	Notification createNotification(UUID messageRef);
}
