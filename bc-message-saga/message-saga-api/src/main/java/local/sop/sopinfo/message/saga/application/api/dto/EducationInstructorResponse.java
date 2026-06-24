package local.sop.sopinfo.message.saga.application.api.dto;
import java.time.Instant;

import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;

public record EducationInstructorResponse(
        CompositeKey id,
        Instant createdAt,
        boolean isActive
) {}
