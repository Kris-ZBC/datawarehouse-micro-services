package local.sop.sopinfo.anonymize.saga.application.ports.out.anonymize;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;


public interface AnonymizePort {
	UUID create(UUID personRef);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
