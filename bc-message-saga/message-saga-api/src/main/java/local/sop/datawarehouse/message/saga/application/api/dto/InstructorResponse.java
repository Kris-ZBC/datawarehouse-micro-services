package local.sop.datawarehouse.message.saga.application.api.dto;

import java.util.UUID;

public record InstructorResponse(
        UUID id,
        UUID personRef
) {}
