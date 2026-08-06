package local.sop.datawarehouse.registration.saga.application.infrastructure.auditlog;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.registration.saga.application.api.dto.auditlog.AuditlogResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.auditlog.CreateAuditlogCmd;
import local.sop.datawarehouse.registration.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

@Component
public class AuditlogHttpAdapter implements AuditlogPort {

	private final RestClient auditlog;

	public AuditlogHttpAdapter(@Qualifier("auditlog") RestClient auditlog) {
		this.auditlog = auditlog;
	}

	@Override
    public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        return auditlog.put()
            .uri("/internal/auditlogs/{id}/compensate/create", id)
            .body(new PayloadCompensateCreate(clazz, sagaState))
            .retrieve()
            .body(ResponseCompensated.class);
    }

    @Override
    public UUID create(CreateAuditlogCmd payload) {

        return auditlog.post()
            .uri("/internal/auditlogs")
            .body(payload)
            .retrieve()
            .body(UUID.class);
    }

    @Override
    public AuditlogResponse getById(UUID id) {
        return auditlog.get()
            .uri("/internal/auditlogs/{id}", id)
            .retrieve()
            .body(AuditlogResponse.class);
    }
}
