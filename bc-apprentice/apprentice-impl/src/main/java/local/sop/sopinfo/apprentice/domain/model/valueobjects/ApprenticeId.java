package local.sop.sopinfo.apprentice.domain.model.valueobjects;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.valueobjects.DomainId;
import local.sop.common.libs.sharedkernel.valueobjects.utils.UUIDUtil;

public record ApprenticeId(UUID value) implements DomainId {
    public ApprenticeId {
        if (value == null) {
            throw new IllegalArgumentException("ApprenticeId cannot be null");
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public static ApprenticeId of(UUID value) {
        return new ApprenticeId(value);
    }

    public static ApprenticeId newId() {
        return new ApprenticeId(UUIDUtil.newUuid());
    }
}
