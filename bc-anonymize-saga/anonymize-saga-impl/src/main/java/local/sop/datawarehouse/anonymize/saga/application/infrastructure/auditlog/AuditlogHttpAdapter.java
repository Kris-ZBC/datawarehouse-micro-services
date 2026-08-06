package local.sop.datawarehouse.anonymize.saga.application.infrastructure.auditlog;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.anonymize.saga.application.api.dto.ResponseAuditlog;
import local.sop.datawarehouse.anonymize.saga.application.infrastructure.request.PayloadAuditlogCreate;
import local.sop.datawarehouse.anonymize.saga.application.ports.out.auditlog.AuditlogPort;

@Component
public class AuditlogHttpAdapter implements AuditlogPort {

    private final RestClient auditlog;

    public AuditlogHttpAdapter(@Qualifier("auditlog") RestClient auditlog) {
        this.auditlog = auditlog;
    }


    @Override
    public UUID create(UUID actor, ActorType type, Severity severity, String originSystem, String originService,
                       String originComponent, String data, String description) {

        return auditlog.post()
                .uri("/internal/auditlogs/create")
                .body(new PayloadAuditlogCreate(actor, type, severity, originSystem, originService, originComponent, data, description))
                .retrieve()
                .body(ResponseAuditlog.class).id();
    }

    @Override
    public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        return auditlog.put()
                .uri("/internal/auditlogs/{id}/compensate/create", id)
                .body(new PayloadCompensateCreate(clazz, sagaState))
                .retrieve()
                .body(ResponseCompensated.class);
    }


}
