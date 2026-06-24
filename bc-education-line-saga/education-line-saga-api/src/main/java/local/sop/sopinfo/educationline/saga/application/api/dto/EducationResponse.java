package local.sop.sopinfo.educationline.saga.application.api.dto;

import java.util.UUID;

public record EducationResponse(
    UUID id,
    String name,
    String category,
    Boolean isActive
) { }
