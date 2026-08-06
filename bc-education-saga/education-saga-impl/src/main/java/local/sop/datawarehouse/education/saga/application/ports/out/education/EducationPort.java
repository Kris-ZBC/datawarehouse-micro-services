package local.sop.datawarehouse.education.saga.application.ports.out.education;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.education.saga.application.api.dto.*;

public interface EducationPort {
    UUID createEducation(CreateEducationCmd cmd);
    EducationResponse findEducationById(UUID educationId);
    EducationResponse updateEducationName(UUID educationId, String name);
    EducationResponse updateEducationCategory(UUID educationId, String category);
    EducationResponse activateEducation(UUID educationId);
    EducationResponse deactivateEducation(UUID educationId);
    /* ────────────── Compensate methods ────────────── */
    ResponseCompensated compensateCreateEducation(UUID id, Class<?> clazz, SagaOutcome sagaState);
    ResponseCompensated compensateUpdateEducationName(UUID id, Class<?> clazz, SagaOutcome sagaState, String previousName);
    ResponseCompensated compensateUpdateEducationCategory(UUID id, Class<?> clazz, SagaOutcome sagaState, String previousCategory);
    ResponseCompensated compensateActivateEducation(UUID id, Class<?> clazz, SagaOutcome sagaState);
    ResponseCompensated compensateDeactivateEducation(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
