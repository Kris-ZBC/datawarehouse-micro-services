package local.sop.datawarehouse.educationline.saga.application.ports.out.educationline;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.datawarehouse.educationline.saga.application.api.dto.EducationLineResponse;
import local.sop.datawarehouse.educationline.saga.application.infrastructure.response.ResponseCompensated;

public interface EducationLinePort {
    UUID createEducationLine(String name, int durationYears, int durationMonths, int durationDays, UUID educationRef);
    UUID updateEducationLineName(UUID id, String name);
    EducationLineResponse findEducationLineById(UUID id);
    UUID updateEducationLineDuration(UUID id, int durationYears, int durationMonths, int durationDays);
    UUID deactivateEducationLine(UUID id);
    UUID activateEducationLine(UUID id);
    ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
	ResponseCompensated compensateUpdateName(UUID id, Class<?> clazz, SagaOutcome sagaState, String previousName);
	ResponseCompensated compensateUpdateDuration(UUID id, Class<?> clazz, SagaOutcome sagaState , int previousDurationYears, int previousDurationMonths, int previousDurationDays);
	ResponseCompensated compensateActivate(UUID id, Class<?> clazz, SagaOutcome sagaState);
	ResponseCompensated compensateDeactivate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
