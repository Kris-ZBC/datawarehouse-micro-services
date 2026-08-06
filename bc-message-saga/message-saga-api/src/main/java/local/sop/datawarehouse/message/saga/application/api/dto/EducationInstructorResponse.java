package local.sop.datawarehouse.message.saga.application.api.dto;
import java.time.Instant;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

public record EducationInstructorResponse(
        CompositeKey id,
        Instant createdAt,
        boolean isActive
) {}
