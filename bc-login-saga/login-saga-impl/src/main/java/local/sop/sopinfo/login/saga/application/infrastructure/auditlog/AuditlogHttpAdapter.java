package local.sop.sopinfo.login.saga.application.infrastructure.auditlog;


import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.login.saga.application.api.dto.AuditlogResponse;
import local.sop.sopinfo.login.saga.application.infrastructure.request.PayloadAuditLogCreate;
import local.sop.sopinfo.login.saga.application.infrastructure.request.PayloadCompensate;
import local.sop.sopinfo.login.saga.application.infrastructure.response.ResponseCompensated;
import local.sop.sopinfo.login.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

@Component
public class AuditlogHttpAdapter implements AuditlogPort {

	private final RestClient auditlog;

	public AuditlogHttpAdapter(@Qualifier("auditlog") RestClient auditlog) {
		this.auditlog = auditlog;
	}

	@Override
    public UUID create(UUID actorRef, ActorType actorType, Severity severity, String originSystem, String originService,
		String originComponent, String data, String description) {

        AuditlogResponse response = auditlog.post()
            .uri("/internal/auditlogs/create")
            .body(new PayloadAuditLogCreate(actorRef, actorType, severity, originSystem, originService, originComponent, data, description))
            .retrieve()
            .body(AuditlogResponse.class);
        if (response == null) {
            throw new RuntimeException("auditlog.empty.response");
        }
        return response.id();
    }

	@Override
    public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        return auditlog.post()
            .uri("/internal/auditlogs/compensate")
            .body(new PayloadCompensate(id, clazz, sagaState))
            .retrieve()
            .body(ResponseCompensated.class);
    }
}
