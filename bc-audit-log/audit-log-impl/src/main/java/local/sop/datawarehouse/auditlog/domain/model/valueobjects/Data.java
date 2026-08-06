package local.sop.datawarehouse.auditlog.domain.model.valueobjects;

import java.util.Map;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

/**
 * Arbitrary event payload. Must contain a caller identifier and be valid JSON upstream.
 */
public record Data(String json) {

    public Data {
        if (json == null || json.isBlank()) {
            throw new ValidationException("log.data.required", Map.of("field", "data"));
        }
    }

    public static Data newData(String input) {
		if (input == null || input.isBlank()) {
			throw new ValidationException("log.data.required", Map.of("field", "data"));
		}
        return new Data(input);
    }

    @Override
    public String toString() {
        return json;
    }
}
