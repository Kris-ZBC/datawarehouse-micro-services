package local.sop.sopinfo.auditlog.application.api.dto;

import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

public record CompensatedAuditlogResult(
    SagaOutcome sagaState,
    Boolean success
) {

}
