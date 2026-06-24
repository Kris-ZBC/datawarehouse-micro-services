package local.sop.sopinfo.login.domain.model.valueobjects;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;
import local.sop.sopinfo.sharedkernel.valueobjects.utils.UUIDUtil;

public record PersonRef(UUID value) implements DomainId {
	public PersonRef {
		UUIDUtil.require(value, "personRef");
	}

	public static PersonRef of(UUID uuid) {
		return new PersonRef(uuid);
	}
}
