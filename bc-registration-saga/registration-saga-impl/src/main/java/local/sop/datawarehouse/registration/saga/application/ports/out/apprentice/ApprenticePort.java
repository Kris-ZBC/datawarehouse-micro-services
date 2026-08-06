package local.sop.datawarehouse.registration.saga.application.ports.out.apprentice;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.registration.saga.application.api.dto.apprentice.ApprenticeResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.apprentice.CreateApprenticeCmd;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

public interface ApprenticePort {
	UUID create(CreateApprenticeCmd cmd);
	ApprenticeResponse getById(UUID id);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
