package local.sop.sopinfo.educationinstructor.application.api;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import local.sop.sopinfo.educationinstructor.application.api.dto.CreateEducationInstructorCmd;
import local.sop.sopinfo.educationinstructor.application.api.dto.CreatedEducationInstructorResult;
import local.sop.sopinfo.educationinstructor.application.api.dto.EducationInstructorResponse;
import local.sop.sopinfo.educationinstructor.application.api.dto.ToggleActivateEducationInstructorCmd;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;

public interface EducationInstructorDirectory {
	CreatedEducationInstructorResult create(CreateEducationInstructorCmd cmd);
	List<EducationInstructorResponse> getAll();
	Optional<EducationInstructorResponse> findById(CompositeKey id);
	EducationInstructorResponse toggleActive(ToggleActivateEducationInstructorCmd cmd);
	List<EducationInstructorResponse> getByEducationRef(UUID educationRef);
	List<EducationInstructorResponse> getByInstructorRef(UUID instructorRef);

	//EducationInstructorResponse activateEducationInstructor(CompositeKey id);

	// Compensate methods
	ResponseCompensated compensateCreateEducationInstructor(CompositeKey id, Class<?> clazz, SagaOutcome sagaState);
	ResponseCompensated compensateActivateEducationInstructor(CompositeKey id, Class<?> clazz, SagaOutcome sagaState);
	ResponseCompensated compensateDeactivateEducationInstructor(CompositeKey id, Class<?> clazz, SagaOutcome sagaState);
}
