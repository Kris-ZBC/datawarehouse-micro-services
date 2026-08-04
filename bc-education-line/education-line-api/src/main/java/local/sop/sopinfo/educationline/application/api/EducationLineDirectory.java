package local.sop.sopinfo.educationline.application.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.educationline.application.api.dto.*;
import local.sop.common.libs.sharedkernel.sagas.compensate.Compensatable;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;

public interface EducationLineDirectory extends Compensatable {
	EducationLineResponse createEducationLine(CreateEducationLineCmd cmd);
	List<EducationLineResponse> findAll();
	Optional<EducationLineResponse> findById(UUID id);
	List<EducationLineResponse> findByEducationRef(UUID id);
	EducationLineResponse updateEducationLineName(UUID id, UpdateEducationLineNameCmd request);
	EducationLineResponse updateEducationLineDuration(UUID id, UpdateEducationLineDurationCmd request);
	EducationLineResponse deactivateEducationLine(UUID id);
	EducationLineResponse activateEducationLine(UUID id);
	// ONLY for compensation
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
	ResponseCompensated compensateActivate(UUID id, Class<?> clazz, SagaOutcome saga);
	ResponseCompensated compensateDeactivate(UUID id, Class<?> clazz, SagaOutcome saga);
	ResponseCompensated compensateName(UUID id, Class<?> clazz, SagaOutcome saga, UpdateEducationLineNameCmd nameReq);
	ResponseCompensated compensateDuration(UUID id, Class<?> clazz, SagaOutcome saga, UpdateEducationLineDurationCmd req);
}