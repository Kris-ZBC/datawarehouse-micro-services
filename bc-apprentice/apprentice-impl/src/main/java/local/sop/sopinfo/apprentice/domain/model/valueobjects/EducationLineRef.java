package local.sop.sopinfo.apprentice.domain.model.valueobjects;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;
import local.sop.sopinfo.sharedkernel.valueobjects.utils.UUIDUtil;

public record EducationLineRef(UUID value) implements DomainId {
    public EducationLineRef {
        if (value == null) {
            throw new IllegalArgumentException("EducationLineRef cannot be null");
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public static EducationLineRef newId() {
        return new EducationLineRef(UUIDUtil.newUuid());
    }
}
