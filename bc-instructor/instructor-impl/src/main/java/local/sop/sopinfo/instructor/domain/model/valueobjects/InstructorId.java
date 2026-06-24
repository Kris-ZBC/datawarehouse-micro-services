package local.sop.sopinfo.instructor.domain.model.valueobjects;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;
import local.sop.sopinfo.sharedkernel.valueobjects.utils.UUIDUtil;

public record InstructorId(UUID value) implements DomainId {

    public InstructorId {
        value = UUIDUtil.require(value, "instructor.id");
    }

    public static InstructorId newId() {
        return new InstructorId(UUIDUtil.newUuid());
    }

    public static InstructorId of(UUID value) {
        return new InstructorId(value);
    }

    public static InstructorId fromString(String raw) {
        return new InstructorId(UUIDUtil.parseRequired(raw, "instructor.id"));
    }

    public String asString() {
        return value.toString();
    }
}