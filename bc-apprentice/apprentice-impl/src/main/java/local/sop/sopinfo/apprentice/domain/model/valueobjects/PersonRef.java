package local.sop.sopinfo.apprentice.domain.model.valueobjects;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;
import local.sop.sopinfo.sharedkernel.valueobjects.utils.UUIDUtil;

public record PersonRef(UUID value) implements DomainId {
    public PersonRef {
        if (value == null) {
            throw new IllegalArgumentException("PersonRef cannot be null");
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public static PersonRef newId() {
        return new PersonRef(UUIDUtil.newUuid());
    }
} 
