package local.sop.sopinfo.sharedkernel.sagas.compensate.request;


import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

public record PayloadCompensateCreate(
    Class<?> clazz, SagaOutcome sagaState
) {

}
