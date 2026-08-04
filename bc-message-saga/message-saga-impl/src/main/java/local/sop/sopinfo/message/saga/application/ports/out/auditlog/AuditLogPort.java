package local.sop.sopinfo.message.saga.application.ports.out.auditlog;

import java.util.UUID;

import local.sop.sopinfo.message.saga.application.api.dto.AuditLogResponse;
import local.sop.sopinfo.message.saga.application.api.dto.CreateAuditLogCmd;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;

public interface AuditLogPort {
	UUID create(CreateAuditLogCmd payload);
	AuditLogResponse getById(UUID id);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
