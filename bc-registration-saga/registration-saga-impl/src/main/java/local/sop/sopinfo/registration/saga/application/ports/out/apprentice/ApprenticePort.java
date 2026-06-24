package local.sop.sopinfo.registration.saga.application.ports.out.apprentice;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.sopinfo.registration.saga.application.api.dto.apprentice.ApprenticeResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.apprentice.CreateApprenticeCmd;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

public interface ApprenticePort {
	UUID create(CreateApprenticeCmd cmd);
	ApprenticeResponse getById(UUID id);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
