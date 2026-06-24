package local.sop.sopinfo.education.saga.application.infrastructure.response;

import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

public record ResponseCompensated(
    SagaOutcome sagaState,
    Boolean success
) { }