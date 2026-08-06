package local.sop.datawarehouse.auditlog.application.api.dto;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

public record CompensatedAuditlogResult(
    SagaOutcome sagaState,
    Boolean success
) {

}
