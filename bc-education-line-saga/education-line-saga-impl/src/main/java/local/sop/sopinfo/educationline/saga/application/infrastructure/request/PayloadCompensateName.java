package local.sop.sopinfo.educationline.saga.application.infrastructure.request;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

public record PayloadCompensateName(
    UUID id, Class<?> clazz, SagaOutcome sagaState, String previousName
) {

}
