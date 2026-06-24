package local.sop.sopinfo.sharedkernel.sagas.compensate;


import java.util.UUID;

import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;

public interface Compensatable {
    ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);

}
