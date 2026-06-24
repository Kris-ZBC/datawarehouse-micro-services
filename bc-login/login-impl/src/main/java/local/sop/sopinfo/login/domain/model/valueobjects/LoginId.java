package local.sop.sopinfo.login.domain.model.valueobjects;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;
import local.sop.sopinfo.sharedkernel.valueobjects.utils.UUIDUtil;

public record LoginId(UUID value) implements DomainId{
	public LoginId {
		UUIDUtil.require(value, "LoginId");
	}

	public static LoginId newId() {
		return new LoginId(UUID.randomUUID());
	}

	public static LoginId of(UUID uuid) {
		return new LoginId(uuid);
	}
}
