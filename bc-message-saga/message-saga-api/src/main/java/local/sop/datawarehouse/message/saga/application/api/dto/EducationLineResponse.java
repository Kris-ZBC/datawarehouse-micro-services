package local.sop.datawarehouse.message.saga.application.api.dto;

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
