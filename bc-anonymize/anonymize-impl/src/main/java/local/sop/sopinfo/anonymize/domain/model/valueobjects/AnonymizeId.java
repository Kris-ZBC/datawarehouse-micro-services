package local.sop.sopinfo.anonymize.domain.model.valueobjects;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;
import local.sop.sopinfo.sharedkernel.valueobjects.utils.UUIDUtil;

public record AnonymizeId(UUID value) implements DomainId {

    public AnonymizeId {
        value = UUIDUtil.require(value, "anonymizeid");
    }

    public static AnonymizeId newId() {
        return new AnonymizeId(UUIDUtil.newUuid());
    }

    public static AnonymizeId of(UUID value) {
        return new AnonymizeId(value);
    }

    public static AnonymizeId fromString(String raw) {
        return new AnonymizeId(UUIDUtil.parseRequired(raw, "anonymizeid"));
    }


    @Override
    public String toString() {
        return value.toString();
    }
}