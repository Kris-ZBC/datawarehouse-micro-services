package local.sop.sopinfo.login.saga.application.ports.out.auditlog;

import java.util.UUID;

import local.sop.sopinfo.login.saga.application.infrastructure.response.ResponseCompensated;
import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

public interface AuditlogPort {
	UUID create(UUID actorRef, ActorType actorType, Severity severity, String originSystem, String originService, String originComponent, String data, String description);
	ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
