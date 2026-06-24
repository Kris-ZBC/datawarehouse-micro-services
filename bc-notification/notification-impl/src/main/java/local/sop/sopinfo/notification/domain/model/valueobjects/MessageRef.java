package local.sop.sopinfo.notification.domain.model.valueobjects;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;
import local.sop.sopinfo.sharedkernel.valueobjects.utils.UUIDUtil;

public record MessageRef(UUID value) implements DomainId {
	public MessageRef {
		UUIDUtil.require(value, "messageRef");
	}

	public static MessageRef of(UUID uuid) {
		return new MessageRef(uuid);
	}
}
