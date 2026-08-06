package local.sop.datawarehouse.registration.saga.application.ports.out.instructor;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.registration.saga.application.api.dto.instructor.CreateInstructorCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.instructor.InstructorResponse;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

public interface InstructorPort {
	UUID create(CreateInstructorCmd cmd);
	InstructorResponse getById(UUID id);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
