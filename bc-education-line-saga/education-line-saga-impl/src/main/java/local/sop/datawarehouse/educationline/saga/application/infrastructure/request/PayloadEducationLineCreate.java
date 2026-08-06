package local.sop.datawarehouse.educationline.saga.application.infrastructure.request;

import java.util.UUID;

public record PayloadEducationLineCreate(
        String name, int durationYears, int durationMonths, int durationDays, UUID educationRef
) {}
