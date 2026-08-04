package local.sop.sopinfo.personnotification.domain.model.valueobjects;

import java.time.LocalDateTime;
import java.util.Map;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public record CreatedAtTimestamp(LocalDateTime value) {
    public CreatedAtTimestamp {
        if (value == null) {
            throw new ValidationException("personnotification.createdate.required",
                    Map.of("field", "createdAt", "createdAt", value == null ? "null" : value.toString()));
        }
                
    }
}
