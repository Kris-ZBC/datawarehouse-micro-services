package local.sop.sopinfo.educationinstructor.application.api.dto;

import java.time.LocalDateTime;

import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;

public record EducationInstructorResponse(
	CompositeKey id,
	Boolean isActive,
	LocalDateTime createdAt
) { }
