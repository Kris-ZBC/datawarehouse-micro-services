package local.sop.sopinfo.registration.saga.application.api.dto.instructor;

import java.util.UUID;

public record InstructorResponse(
        UUID id,
        UUID personRef
) {}