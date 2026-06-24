package local.sop.sopinfo.consent.saga.application.ports.out.auditlog;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.enums.ActorType;
import local.sop.sopinfo.sharedkernel.enums.Severity;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;

public interface AuditlogPort {

    UUID create(UUID actor, ActorType type, Severity severity, String originSystem, String originService, String originComponent, String data, String description);
    ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
