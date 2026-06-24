package local.sop.sopinfo.anonymize.saga.application.infrastructure.anonymize;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.anonymize.saga.application.api.dto.AnonymizeResponse;
import local.sop.sopinfo.anonymize.saga.application.infrastructure.request.PayloadAnonymizeCreate;
import local.sop.sopinfo.anonymize.saga.application.ports.out.anonymize.AnonymizePort;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;


@Component
public class AnonymizeHttpAdapter implements AnonymizePort {
	
	private final RestClient anonymize;

	public AnonymizeHttpAdapter(@Qualifier("anonymize") RestClient anonymize) {
		this.anonymize = anonymize;
	}

	@Override
	public UUID create(UUID personRef) {
		return anonymize.post()
			.uri("/internal/anonymizations")
			.body(new PayloadAnonymizeCreate(personRef))
			.retrieve()
			.body(AnonymizeResponse.class).anonymizationId();
	}

	 @Override
    public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        return anonymize.post()
            .uri("/internal/anonymizations/{id}/compensate/create", id)
            .body(new PayloadCompensateCreate(clazz, sagaState))
            .retrieve()
            .body(ResponseCompensated.class);
    }

}
