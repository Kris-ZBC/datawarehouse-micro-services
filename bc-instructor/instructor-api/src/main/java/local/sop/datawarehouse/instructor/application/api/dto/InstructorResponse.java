package local.sop.datawarehouse.instructor.application.api.dto;

import java.util.UUID;

public record InstructorResponse(
        UUID id,
        UUID personRef
) {}