package local.sop.sopinfo.notification.interfaceadapters.persistence.jpa;

import org.springframework.stereotype.Component;

import local.sop.sopinfo.notification.domain.model.Notification;
import local.sop.sopinfo.notification.domain.model.valueobjects.*;

@Component
public final class NotificationMapper {
	public NotificationMapper() {}

	public Notification toDomain(NotificationEntity entity) {
		return Notification.builder()
			.id(NotificationId.of(entity.getId()))
			.messageRef(MessageRef.of(entity.getMessageRef()))
			.seen(entity.getSeen())
			.build();
	}

	public NotificationEntity toEntity(Notification notification) {
		return NotificationEntity.builder()
			.id(notification.getId().value())
			.messageRef(notification.getMessageRef().value())
			.build();
	}
}
