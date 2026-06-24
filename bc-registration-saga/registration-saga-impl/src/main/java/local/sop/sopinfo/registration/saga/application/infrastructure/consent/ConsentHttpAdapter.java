package local.sop.sopinfo.registration.saga.application.infrastructure.consent;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.registration.saga.application.api.dto.consent.ConsentResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.consent.ConsentStatementResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.consent.GrantConsentCmd;
import local.sop.sopinfo.registration.saga.application.ports.out.consent.ConsentPort;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;

@Component
public class ConsentHttpAdapter implements ConsentPort {

	private final RestClient consent;

	public ConsentHttpAdapter(@Qualifier("consent") RestClient consent) {
		this.consent = consent;
	}

	@Override
    public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        return consent.put()
            .uri("/internal/consent/statements/{id}/compensate/create", id)
            .body(new PayloadCompensateCreate(clazz, sagaState))
            .retrieve()
            .body(ResponseCompensated.class);
    }

    @Override
    public ConsentResponse getById(UUID id) {
        return consent.get()
            .uri("/internal/consents/consent/{id}", id)
            .retrieve()
            .body(ConsentResponse.class);
    }

    @Override
    public UUID grant(GrantConsentCmd payload) {
        return consent.post()
            .uri("/internal/consents/consent/grant")
            .body(payload)
            .retrieve()
            .body(UUID.class);
    }

    @Override
    public ConsentStatementResponse getConsentStatementById(UUID id) {
        return consent.get()
            .uri("/internal/consent/statements/statements?id={id}", id)
            .retrieve()
            .body(ConsentStatementResponse.class);  
    }
}