package local.sop.sopinfo.workhour.domain.model.valueobjects;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;
import local.sop.sopinfo.sharedkernel.valueobjects.utils.UUIDUtil;

public record WorkScheduleId(UUID value) implements DomainId {
    public WorkScheduleId {
        UUIDUtil.require(value, "WorkScheduleId");
    }
    public static WorkScheduleId newId() {
        return new WorkScheduleId(UUIDUtil.newUuid());
    }

    public static WorkScheduleId of(String raw, String field) {
        return new WorkScheduleId(UUIDUtil.parseRequired(raw, field));
    }

    public static WorkScheduleId of(UUID uuid) {
        return new WorkScheduleId(uuid);
    }
    public static WorkScheduleId fromString(String raw, String field) {
        return of(raw, field);
    }

}
