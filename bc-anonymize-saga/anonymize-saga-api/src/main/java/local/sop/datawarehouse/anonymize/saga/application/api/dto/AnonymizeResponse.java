package local.sop.datawarehouse.anonymize.saga.application.api.dto;

import java.util.UUID;

public record AnonymizeResponse(
        UUID anonymizationId,
        UUID personRef
) {}