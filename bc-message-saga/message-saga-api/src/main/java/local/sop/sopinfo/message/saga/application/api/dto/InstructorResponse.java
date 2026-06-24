package local.sop.sopinfo.message.saga.application.api.dto;

import java.util.UUID;

public record InstructorResponse(
        UUID id,
        UUID personRef
) {}
