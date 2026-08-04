package local.sop.sopinfo.message.saga.application.infrastructure.auditlog;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.message.saga.application.api.dto.CreateAuditLogCmd;
import local.sop.sopinfo.message.saga.application.api.dto.AuditLogResponse;
import local.sop.sopinfo.message.saga.application.ports.out.auditlog.AuditLogPort;

import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;

@Component
public class AuditLogHttpAdapter implements AuditLogPort {

	private final RestClient auditlog;

	public AuditLogHttpAdapter(@Qualifier("auditlog") RestClient auditLog) {
		this.auditlog = auditLog;
	}

	@Override
	public UUID create(CreateAuditLogCmd payload) {

		return auditlog.post()
			.uri("/internal/auditlogs")
			.body(payload)
			.retrieve()
			.body(UUID.class);
	}

	@Override
	public AuditLogResponse getById(UUID id) {
		return auditlog.get()
			.uri("/internal/auditlogs/{id}", id)
			.retrieve()
			.body(AuditLogResponse.class);
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
