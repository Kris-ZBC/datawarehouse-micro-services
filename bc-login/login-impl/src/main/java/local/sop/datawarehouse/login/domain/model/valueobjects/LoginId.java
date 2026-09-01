package local.sop.datawarehouse.login.domain.model.valueobjects;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.valueobjects.DomainId;
import local.sop.common.libs.sharedkernel.valueobjects.utils.UUIDUtil;

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
