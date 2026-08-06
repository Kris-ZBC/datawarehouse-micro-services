package local.sop.datawarehouse.educationinstructor.application.infrastructure.response;

import java.util.UUID;

public record InstructorResponse(
    UUID id,
    UUID personRef
) { }
