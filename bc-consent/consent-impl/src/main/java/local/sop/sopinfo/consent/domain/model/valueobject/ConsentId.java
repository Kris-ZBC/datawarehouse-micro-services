package local.sop.sopinfo.consent.domain.model.valueobject;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;
import local.sop.sopinfo.sharedkernel.valueobjects.utils.UUIDUtil;

public record ConsentId(UUID value) implements DomainId {
	public ConsentId {
		UUIDUtil.require(value, "value");
		
	}

	public static ConsentId newId() {
		return new ConsentId(UUIDUtil.newUuid());
	}

	public static ConsentId of(String raw, String field) {
		return new ConsentId(UUIDUtil.parseRequired(raw, field));
	}

	public static ConsentId of(UUID uuid) {
		return new ConsentId(uuid);
	}

	public static ConsentId fromString(String raw, String field) {
		return of(raw, field);
	}
}
