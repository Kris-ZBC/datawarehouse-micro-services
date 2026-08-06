package local.sop.datawarehouse.educationinstructor.application.api.dto;

import java.time.LocalDateTime;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

public record EducationInstructorResponse(
	CompositeKey id,
	Boolean isActive,
	LocalDateTime createdAt
) { }
