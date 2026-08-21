package local.sop.datawarehouse.registration.saga.application.infrastructure.consentsaga;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.registration.saga.application.api.dto.consent.ConsentResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.consent.GrantConsentCmd;
import local.sop.datawarehouse.registration.saga.application.ports.out.consentsaga.ConsentSagaPort;

@Component
public class ConsentSagaHttpAdapter implements ConsentSagaPort {

	private final RestClient consentSaga;

	public ConsentSagaHttpAdapter(@Qualifier("consent-saga") RestClient consentSaga) {
		this.consentSaga = consentSaga;
	}

	@Override
    public ResponseCompensated compensateConsent(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        return consentSaga.post()
            .uri("/internal/saga/consents/consent/{id}/compensate/create", id)
            .body(new PayloadCompensateCreate(clazz, sagaState))
            .retrieve()
            .body(ResponseCompensated.class);
    }

    @Override
    public ConsentResponse grant(GrantConsentCmd payload) {
        return consentSaga.post()
            .uri("/internal/saga/consents/consent/grant")
            .body(payload)
            .retrieve()
            .body(ConsentResponse.class);
    }
}