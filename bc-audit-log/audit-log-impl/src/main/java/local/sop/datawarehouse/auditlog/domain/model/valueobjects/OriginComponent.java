package local.sop.datawarehouse.auditlog.domain.model.valueobjects;

import java.util.Map;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public final class OriginComponent {

    private final String value;

    public OriginComponent(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("log.origin.component.required", Map.of("field", "originComponent"));
        }
        this.value = value;
    }

    public static OriginComponent newOriginComponent(String value) {
        return new OriginComponent(value);
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}