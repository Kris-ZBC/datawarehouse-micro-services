package local.sop.sopinfo.sharedkernel.sagas.compensate.request;

import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

public record PayloadCompensateUpdate(
    Class<?> clazz,
    SagaOutcome sagaOutcome,
    String previousValue,  // whatever was there before
    Boolean previousActive
) {

}
