package local.sop.datawarehouse.sopinstructor.domain.model.valueobjects;

import java.util.UUID;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;
import local.sop.common.libs.sharedkernel.valueobjects.utils.UUIDUtil;

public record SopRef(UUID value) implements DomainId {
    public SopRef {
        UUIDUtil.require(value, "value");
    }

    public static SopRef newId() {
        return new SopRef(UUIDUtil.newUuid());
    }

    public static SopRef of(String raw, String field) {
        return new SopRef(UUIDUtil.parseRequired(raw, field));
    }

    public static SopRef of(UUID uuid) {
        return new SopRef(uuid);
    }

    public static SopRef fromString(String raw, String field) {
        return of(raw, field);
    }
}
