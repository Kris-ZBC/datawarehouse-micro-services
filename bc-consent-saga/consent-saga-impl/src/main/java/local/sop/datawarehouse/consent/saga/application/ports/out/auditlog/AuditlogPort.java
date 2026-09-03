package local.sop.datawarehouse.consent.saga.application.ports.out.auditlog;

import java.util.UUID;

import local.sop.datawarehouse.sharedlib.enums.ActorType;
import local.sop.datawarehouse.sharedlib.enums.Severity;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;

public interface AuditlogPort {

    UUID create(UUID actor, ActorType type, Severity severity, String originSystem, String originService, String originComponent, String data, String description);
    ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState);
}
