package local.sop.sopinfo.instructor.domain.model.valueobjects;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;
import local.sop.sopinfo.sharedkernel.valueobjects.utils.UUIDUtil;

public record PersonRef(UUID value) implements DomainId {

    public PersonRef {
        value = UUIDUtil.require(value, "instructor.personRef");
    }

    public static PersonRef of(UUID value) {
        return new PersonRef(value);
    }

    public static PersonRef fromString(String raw) {
        return new PersonRef(UUIDUtil.parseRequired(raw, "instructor.personRef"));
    }
}