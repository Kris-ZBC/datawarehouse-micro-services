package local.sop.datawarehouse.consent.domain.model.valueobject;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.valueobjects.DomainId;
import local.sop.common.libs.sharedkernel.valueobjects.utils.UUIDUtil;

public record PersonRef(UUID value) implements DomainId {
    public PersonRef {
        UUIDUtil.require(value, "value");
	}

	public static PersonRef of(UUID uuid) {
		return new PersonRef(uuid);
	}
}
