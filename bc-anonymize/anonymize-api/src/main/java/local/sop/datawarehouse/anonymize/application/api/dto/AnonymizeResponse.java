package local.sop.datawarehouse.anonymize.application.api.dto;

import java.util.UUID;

public record AnonymizeResponse(
        UUID anonymizationId,
        UUID personRef
) {}