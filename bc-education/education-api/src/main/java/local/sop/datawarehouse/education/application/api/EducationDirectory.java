package local.sop.datawarehouse.education.application.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.education.application.api.dto.CreateEducationCmd;
import local.sop.datawarehouse.education.application.api.dto.UpdateEducationNameCmd;
import local.sop.datawarehouse.education.application.api.dto.UpdateEducationCategoryCmd;
import local.sop.datawarehouse.education.application.api.dto.EducationResponse;

public interface EducationDirectory {
    EducationResponse createEducation(CreateEducationCmd cmd);
    EducationResponse updateEducationName(UUID id, UpdateEducationNameCmd cmd);
    EducationResponse updateEducationCategory(UUID id, UpdateEducationCategoryCmd cmd);
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