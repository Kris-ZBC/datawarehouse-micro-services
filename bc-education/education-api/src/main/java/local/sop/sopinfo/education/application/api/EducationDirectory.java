package local.sop.sopinfo.education.application.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.education.application.api.dto.CreateEducationCmd;
import local.sop.sopinfo.education.application.api.dto.EducationResponse;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;

public interface EducationDirectory {
    EducationResponse createEducation(CreateEducationCmd cmd);
    EducationResponse updateEducationName(UUID id, String name);
    EducationResponse updateEducationCategory(UUID id, String category);
    Optional<EducationResponse> findById(UUID id);
    List<EducationResponse> findAll();
    EducationResponse activateEducation(UUID id);
    EducationResponse deactivateEducation(UUID id);

    // Compensate methods
    ResponseCompensated compensateCreateEducation(UUID id, Class<?> clazz, SagaOutcome sagaState);
    ResponseCompensated compensateUpdateEducationName(UUID id, Class<?> clazz, SagaOutcome sagaState, String previousName);
    ResponseCompensated compensateUpdateEducationCategory(UUID id, Class<?> clazz, SagaOutcome sagaState, String previousCategory);
    ResponseCompensated compensateActivateEducation(UUID id, Class<?> clazz, SagaOutcome sagaState);
    ResponseCompensated compensateDeactivateEducation(UUID id, Class<?> clazz, SagaOutcome sagaState);
}