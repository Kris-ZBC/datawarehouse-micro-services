package local.sop.sopinfo.consent.application.api.dto;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

public record CompensatedConsentStatementResult(
    SagaOutcome sagaState,
    Boolean result
) {}
