package local.sop.sopinfo.education.saga.application.api.dto;

import java.util.UUID;

public record EducationResponse(
    UUID id,
    String name,
    String category,
    boolean active
) {}
