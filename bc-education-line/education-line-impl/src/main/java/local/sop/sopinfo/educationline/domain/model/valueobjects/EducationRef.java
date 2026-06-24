package local.sop.sopinfo.educationline.domain.model.valueobjects;

import java.util.UUID;
import java.util.Map;

import local.sop.sopinfo.sharedkernel.valueobjects.utils.UUIDUtil;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;

public record EducationRef(UUID value) implements DomainId{

	public EducationRef {
		if (value == null) {
			throw new ValidationException("Key.required", Map.of("field", "educationRef"));
		}
	}

	@Override
	public UUID value() {
		return value;
	}

	public static EducationRef newEducationRef() {
		return new EducationRef(UUIDUtil.newUuid());
	}

	public String asString() {
		return value.toString();
	}
}
