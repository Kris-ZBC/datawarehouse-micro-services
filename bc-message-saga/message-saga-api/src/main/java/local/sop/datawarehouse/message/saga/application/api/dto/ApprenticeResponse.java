package local.sop.datawarehouse.message.saga.application.api.dto;

import java.util.UUID;

public record ApprenticeResponse(
    UUID apprenticeId,
    UUID personRef,
    UUID educationLineRef
) {}
