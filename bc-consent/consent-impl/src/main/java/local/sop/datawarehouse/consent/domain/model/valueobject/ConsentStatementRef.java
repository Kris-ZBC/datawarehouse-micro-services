package local.sop.datawarehouse.consent.domain.model.valueobject;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.valueobjects.DomainId;
import local.sop.common.libs.sharedkernel.valueobjects.utils.UUIDUtil;

public record ConsentStatementRef(UUID value) implements DomainId {
	public ConsentStatementRef {
		UUIDUtil.require(value, "value");
	}
	public UUID value() {
		return value;
	}

	public static ConsentStatementRef newId() {
		return new ConsentStatementRef(UUIDUtil.newUuid());
	}

	public static ConsentStatementRef of(String raw, String field) {
		return new ConsentStatementRef(UUIDUtil.parseRequired(raw, field));
	}

	public static ConsentStatementRef of(UUID uuid) {
		return new ConsentStatementRef(uuid);
	}

	public static ConsentStatementRef fromString(String raw, String field) {
		return of(raw, field);
	}
}
