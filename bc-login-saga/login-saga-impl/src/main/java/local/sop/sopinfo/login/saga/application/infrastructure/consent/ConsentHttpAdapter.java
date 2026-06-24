package local.sop.sopinfo.login.saga.application.infrastructure.consent;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.login.saga.application.infrastructure.request.PayloadCompensate;
import local.sop.sopinfo.login.saga.application.infrastructure.response.ResponseCompensated;
import local.sop.sopinfo.login.saga.application.ports.out.consent.ConsentPort;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

@Component
public class ConsentHttpAdapter implements ConsentPort {

	private final RestClient consent;

	public ConsentHttpAdapter(@Qualifier("consent") RestClient consent) {
		this.consent = consent;
	}

	@Override
	public boolean hasConsent(UUID personRef) {
		Boolean result = consent.get()
			.uri("/internal/consents/check/{personRef}", personRef)
			.retrieve()
			.body(Boolean.class);
		return result != null && result;
	}

	@Override
	public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
		return consent.post()
			.uri("/internal/consents/compensate")
			.body(new PayloadCompensate(id, clazz, sagaState))
			.retrieve()
			.body(ResponseCompensated.class);
	}
}
