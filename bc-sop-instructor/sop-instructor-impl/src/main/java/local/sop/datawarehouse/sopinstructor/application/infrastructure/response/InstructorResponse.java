package local.sop.datawarehouse.sopinstructor.application.infrastructure.response;

import java.util.UUID;

public record InstructorResponse(
        UUID id,
        UUID personRef
) {}