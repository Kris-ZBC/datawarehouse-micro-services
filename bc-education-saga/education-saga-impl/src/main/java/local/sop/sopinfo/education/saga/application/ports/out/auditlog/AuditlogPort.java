package local.sop.sopinfo.education.saga.application.ports.out.auditlog;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.education.saga.application.infrastructure.response.ResponseCompensated;
import local.sop.sopinfo.sharedkernel.enums.ActorType;
import local.sop.sopinfo.sharedkernel.enums.Severity;

public interface AuditlogPort {
    UUID create(UUID actor, ActorType type, Severity severity, String originSystem, String originService, String originComponent, String data, String description);
	UUID findById(UUID id);
    /* ────────────── Compensate methods ────────────── */
    ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
