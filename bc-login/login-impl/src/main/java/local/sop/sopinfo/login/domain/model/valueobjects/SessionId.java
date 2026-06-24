package local.sop.sopinfo.login.domain.model.valueobjects;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;
import local.sop.sopinfo.sharedkernel.valueobjects.utils.UUIDUtil;

public record SessionId(UUID value) implements DomainId {
	public SessionId {
		UUIDUtil.require(value, "SessionId");
	}

	public static SessionId newId() {
		return new SessionId(UUID.randomUUID());
	}

	public static SessionId of(UUID uuid) {
		return new SessionId(uuid);
	}
}
