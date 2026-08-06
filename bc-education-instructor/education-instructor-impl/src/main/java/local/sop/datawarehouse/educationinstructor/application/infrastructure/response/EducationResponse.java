package local.sop.datawarehouse.educationinstructor.application.infrastructure.response;

import java.util.UUID;

public record EducationResponse(
    UUID id,
    String name,
    String category,
    Boolean isActive
) { }
