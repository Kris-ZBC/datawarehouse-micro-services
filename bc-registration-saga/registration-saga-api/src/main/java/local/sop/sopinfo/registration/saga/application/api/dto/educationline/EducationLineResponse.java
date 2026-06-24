package local.sop.sopinfo.registration.saga.application.api.dto.educationline;

import java.time.Instant;
import java.util.UUID;

public record EducationLineResponse(
	UUID id,
	String name,
	Integer durationYears,
	Integer durationMonths,
	Integer durationDays,
	UUID educationRef,
	Instant createdAt,
	Boolean isActive
) {}
