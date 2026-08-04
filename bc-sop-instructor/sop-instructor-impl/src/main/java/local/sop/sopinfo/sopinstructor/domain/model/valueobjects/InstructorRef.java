package local.sop.sopinfo.sopinstructor.domain.model.valueobjects;

import java.util.UUID;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;
import local.sop.common.libs.sharedkernel.valueobjects.utils.UUIDUtil;

public record InstructorRef(UUID value) implements DomainId {
    public InstructorRef {
        UUIDUtil.require(value, "value");
    }

    public static InstructorRef newId() {
        return new InstructorRef(UUIDUtil.newUuid());
    }

    public static InstructorRef of(String raw, String field) {
        return new InstructorRef(UUIDUtil.parseRequired(raw, field));
    }

    public static InstructorRef of(UUID uuid) {
        return new InstructorRef(uuid);
    }

    public static InstructorRef fromString(String raw, String field) {
        return of(raw, field);
    }
}
