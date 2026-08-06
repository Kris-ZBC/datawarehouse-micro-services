package local.sop.datawarehouse.anonymize.saga.application.ports.out.anonymize;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;


public interface AnonymizePort {
	UUID create(UUID personRef);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
