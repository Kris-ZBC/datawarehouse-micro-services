package local.sop.datawarehouse.education.domain.model.valueobjects;

import java.util.Map;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;
import local.sop.common.libs.sharedkernel.valueobjects.utils.UUIDUtil;


public record EducationId(UUID value) implements DomainId{

    public EducationId {
        UUIDUtil.require(value, "educationId");
    }

    @Override
    public UUID value() {
        return value;
    }

    public String asString() {
        return value.toString();
    }

    public static EducationId newId() {
        return new EducationId(UUID.randomUUID());
    }

    public static EducationId parse(String raw) {
        return new EducationId(UUIDUtil.parseRequired(raw, "educationId"));
    }

    public ValidationException missingException() {
        return new ValidationException("education.id.required", Map.of("field", "educationId"));
    }
}