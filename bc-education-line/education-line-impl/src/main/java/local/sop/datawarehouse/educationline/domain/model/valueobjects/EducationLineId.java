package local.sop.datawarehouse.educationline.domain.model.valueobjects;

import java.util.Map;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.valueobjects.utils.UUIDUtil;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;

public record EducationLineId(UUID value) implements DomainId {

	public EducationLineId {
		UUIDUtil.require(value, "educationLineId");
	}

	@Override
	public UUID value() {
		return value;
	}

	public String asString() {
		return value.toString();
	}

	public static EducationLineId newId() {
		return new EducationLineId(UUID.randomUUID());
	}
	
	public static EducationLineId parse(String raw) {
		return new EducationLineId(UUIDUtil.parseRequired(raw, "educationLineId"));
	}

	public ValidationException missingException() {
		return new ValidationException("educationline.id.required", Map.of("field", "educationLineId"));
	}

}
