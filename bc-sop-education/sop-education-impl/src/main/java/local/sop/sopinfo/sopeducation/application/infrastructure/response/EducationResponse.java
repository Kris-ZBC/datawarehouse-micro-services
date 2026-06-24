package local.sop.sopinfo.sopeducation.application.infrastructure.response;

import java.util.UUID;

public record EducationResponse(
        UUID id,
        String name,
        String category,
        Boolean isActive
) {}
