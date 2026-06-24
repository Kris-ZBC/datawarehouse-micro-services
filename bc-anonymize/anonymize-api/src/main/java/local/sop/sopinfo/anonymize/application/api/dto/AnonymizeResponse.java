package local.sop.sopinfo.anonymize.application.api.dto;

import java.util.UUID;

public record AnonymizeResponse(
        UUID anonymizationId,
        UUID personRef
) {}