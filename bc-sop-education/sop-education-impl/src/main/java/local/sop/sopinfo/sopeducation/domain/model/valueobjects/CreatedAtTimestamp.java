package local.sop.sopinfo.sopeducation.domain.model.valueobjects;

import java.time.LocalDateTime;
import java.util.Map;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public record CreatedAtTimestamp(LocalDateTime value) {
    public CreatedAtTimestamp {
        if (value == null) {
            throw new ValidationException("sop-education.createdat.required",
                    Map.of("field", "createdAt", "createdAt", value == null ? "null" : value.toString()));
        }

        LocalDateTime now = LocalDateTime.now();
        if (value.isAfter(now)) {
            throw new ValidationException("sop-education.createdat.isafter", Map.of("createdAt", value));
        }
    }

}
