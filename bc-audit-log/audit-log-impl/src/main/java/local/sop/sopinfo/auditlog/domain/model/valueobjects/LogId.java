package local.sop.sopinfo.auditlog.domain.model.valueobjects;

import java.util.Map;
import java.util.UUID;

import local.sop.sopinfo.auditlog.domain.model.Log;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;
import local.sop.sopinfo.sharedkernel.valueobjects.utils.UUIDUtil;

/**
 * Strongly-typed identifier for {@link Log}.
 */
public record LogId(UUID value) implements DomainId {

    public static LogId newId() {
        return new LogId(UUIDUtil.newUuid());
    }

    public LogId {
        UUIDUtil.require(value, "log.id");
    }

    @Override
    public UUID value() {
        return value;
    }

    public String asString() {
        return value.toString();
    }

    public static LogId parse(String raw) {
        return new LogId(UUIDUtil.parseRequired(raw, "log.id"));
    }

    	public static LogId of(UUID uuid) {
		return new  LogId(uuid);
	}


    public ValidationException missingException() {
        return new ValidationException("log.id.required", Map.of("field", "log.id"));
    }
}
