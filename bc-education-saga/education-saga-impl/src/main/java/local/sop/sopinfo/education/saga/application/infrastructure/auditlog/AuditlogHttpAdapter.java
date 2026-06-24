package local.sop.sopinfo.education.saga.application.infrastructure.auditlog;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.education.saga.application.api.dto.ResponseAuditlog;
import local.sop.sopinfo.education.saga.application.infrastructure.request.PayloadAuditlogCreate;
import local.sop.sopinfo.education.saga.application.infrastructure.request.PayloadCreateCompensate;
import local.sop.sopinfo.education.saga.application.infrastructure.response.ResponseCompensated;
import local.sop.sopinfo.education.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.sopinfo.sharedkernel.enums.ActorType;
import local.sop.sopinfo.sharedkernel.enums.Severity;
import local.sop.sopinfo.sharedkernel.exceptions.ConflictException;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

public class AuditlogHttpAdapter implements AuditlogPort {

    private final RestClient auditlog;

    public AuditlogHttpAdapter(@Qualifier("auditlog") RestClient auditlog) {
        this.auditlog = auditlog;
    }

    @Override
    public UUID create(UUID actor, ActorType type, Severity severity, String originSystem, String originService,
            String originComponent, String data, String description) {
        ResponseAuditlog response = auditlog.post()
                .uri("/internal/auditlogs/create")
                .body(new PayloadAuditlogCreate(actor, type, severity, originSystem, originService, originComponent, data, description))
                .retrieve()
                .body(ResponseAuditlog.class);

        if (response == null || response.id() == null) {
            throw new ConflictException("auditlog.creation.failed", Map.of("originSystem", originSystem));
        }

        return response.id();
    }

    @Override
	public UUID findById(UUID id) {
		ResponseAuditlog response = auditlog.get()
			.uri("/internal/auditlogs/{id}", id)
			.retrieve()
			.body(ResponseAuditlog.class);
		
		if (response == null) {
			throw new ConflictException("auditlog.not.found", Map.of("id", id.toString()));
		}
		
		return response.id();
	}

    @Override
    public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        ResponseCompensated response = auditlog.post()
            .uri("/internal/auditlogs/compensate")
            .body(new PayloadCreateCompensate(id, clazz, sagaState))
            .retrieve()
            .body(ResponseCompensated.class);
        
        if (response == null) {
            throw new ConflictException("auditlog.compensation.failed", Map.of("id", id.toString()));
        }
        
        return response;
    }
}
