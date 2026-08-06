package local.sop.datawarehouse.education.saga.application.ports.out.educationinstructor;

import java.util.Optional;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.education.saga.application.api.dto.CreateEducationInstructorCmd;
import local.sop.datawarehouse.education.saga.application.api.dto.EducationInstructorResponse;
public interface EducationInstructorPort {
    EducationInstructorResponse createEducationInstructor(CreateEducationInstructorCmd cmd);
	Optional<EducationInstructorResponse> findById(CompositeKey id);
	EducationInstructorResponse deactivateEducationInstructor(CompositeKey id);
	EducationInstructorResponse activateEducationInstructor(CompositeKey id);
	/* ────────────── Compensate methods ────────────── */
	ResponseCompensated compensateCreateEducationInstructor(CompositeKey id, Class<?> clazz, SagaOutcome sagaState);
	ResponseCompensated compensateActivateEducationInstructor(CompositeKey id, Class<?> clazz, SagaOutcome sagaState);
	ResponseCompensated compensateDeactivateEducationInstructor(CompositeKey id, Class<?> clazz, SagaOutcome sagaState);
}
