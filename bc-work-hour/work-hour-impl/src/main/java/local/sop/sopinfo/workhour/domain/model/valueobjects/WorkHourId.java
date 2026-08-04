package local.sop.sopinfo.workhour.domain.model.valueobjects;

import java.util.Map;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;

public record WorkHourId(UUID value) implements DomainId {

    public WorkHourId {
        if (value == null) {
             throw new ValidationException("workhour.id.required",Map.of("field", "id"));
        }
    }

    public static WorkHourId newId() {
        return new WorkHourId(UUID.randomUUID());
    }

    public static WorkHourId of(UUID value) {
        return new WorkHourId(value);
    }

}
