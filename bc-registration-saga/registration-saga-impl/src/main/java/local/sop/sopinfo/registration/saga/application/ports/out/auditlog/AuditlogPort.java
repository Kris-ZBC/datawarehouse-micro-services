package local.sop.sopinfo.registration.saga.application.ports.out.auditlog;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.sopinfo.registration.saga.application.api.dto.auditlog.AuditlogResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.auditlog.CreateAuditlogCmd;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

public interface AuditlogPort {
	UUID create(CreateAuditlogCmd payload);
	AuditlogResponse getById(UUID id);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
