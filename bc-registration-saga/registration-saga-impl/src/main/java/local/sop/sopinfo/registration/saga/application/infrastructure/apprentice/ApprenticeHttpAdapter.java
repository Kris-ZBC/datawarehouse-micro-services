package local.sop.sopinfo.registration.saga.application.infrastructure.apprentice;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.registration.saga.application.api.dto.apprentice.ApprenticeResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.apprentice.CreateApprenticeCmd;
import local.sop.sopinfo.registration.saga.application.ports.out.apprentice.ApprenticePort;

import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;

@Component
public class ApprenticeHttpAdapter implements ApprenticePort {

	private final RestClient apprentice;
    
	public ApprenticeHttpAdapter(@Qualifier("apprentice") RestClient apprentice) {
		this.apprentice = apprentice;
	}

	@Override
    public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        return apprentice.put()
            .uri("/internal/apprentices/{id}/compensate/create", id)
            .body(new PayloadCompensateCreate(clazz, sagaState))
            .retrieve()
            .body(ResponseCompensated.class);
    }

    @Override
    public UUID create(CreateApprenticeCmd cmd) {
        return apprentice.post()
            .uri("/internal/apprentices")
            .body(cmd)
            .retrieve()
            .body(UUID.class);
    }

    @Override
    public ApprenticeResponse getById(UUID id) {
        return apprentice.get()
            .uri("/internal/apprentices/{id}", id)
            .retrieve()
            .body(ApprenticeResponse.class);
    }
}