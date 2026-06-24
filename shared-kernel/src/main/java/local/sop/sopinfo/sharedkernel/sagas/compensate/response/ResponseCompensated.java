package local.sop.sopinfo.sharedkernel.sagas.compensate.response;

import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

public record ResponseCompensated(
    SagaOutcome sagaState,
    Boolean success
) {

}
