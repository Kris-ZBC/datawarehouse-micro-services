package local.sop.datawarehouse.login.domain.model.valueobjects;

import java.util.Map;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public record IsAccepted(Boolean value) {
    public IsAccepted {
        if (value == null) {
            throw new ValidationException("isaccepted.value.invalid", Map.of("field", "value"));
        }
    }

    public static IsAccepted of(Boolean value) {
        return new IsAccepted(value);
    }
}
