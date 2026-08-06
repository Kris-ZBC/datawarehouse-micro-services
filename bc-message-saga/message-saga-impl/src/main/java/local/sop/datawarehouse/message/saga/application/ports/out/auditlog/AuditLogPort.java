package local.sop.datawarehouse.message.saga.application.ports.out.auditlog;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.message.saga.application.api.dto.AuditLogResponse;
import local.sop.datawarehouse.message.saga.application.api.dto.CreateAuditLogCmd;

public interface AuditLogPort {
	UUID create(CreateAuditLogCmd payload);
	AuditLogResponse getById(UUID id);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
