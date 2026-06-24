package local.sop.sopinfo.message.saga.application.ports.out.personnotification;

import java.util.List;
import java.util.UUID;

import local.sop.sopinfo.message.saga.application.api.dto.PersonNotificationResponse;

public interface PersonNotificationPort {
	void create(UUID personRef, UUID notificationRef);
	List<PersonNotificationResponse> findByNotificationRef(UUID notificationRef);
}
