package local.sop.datawarehouse.anonymize.application.api.dto;

import java.util.UUID;


public record FetchByParamsQuery(
        UUID anonymizationId,
        UUID personRef
) {}