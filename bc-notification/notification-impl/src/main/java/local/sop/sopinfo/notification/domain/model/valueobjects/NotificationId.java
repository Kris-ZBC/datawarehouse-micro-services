package local.sop.sopinfo.notification.domain.model.valueobjects;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;
import local.sop.sopinfo.sharedkernel.valueobjects.utils.UUIDUtil;

public record NotificationId(UUID value) implements DomainId{
	public NotificationId {
		UUIDUtil.require(value, "NotificationId");
	}

	public static NotificationId newId() {
		return new NotificationId(UUIDUtil.newUuid());
	}

	public static NotificationId of(UUID uuid) {
		return new NotificationId(uuid);
	}
}
