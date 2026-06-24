package local.sop.sopinfo.message.saga.application.infrastructure.apprentice;

import java.util.UUID;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.message.saga.application.ports.out.apprentice.ApprenticePort;
import local.sop.sopinfo.message.saga.application.api.dto.ApprenticeResponse;

@Component
public class ApprenticeHttpAdapter implements ApprenticePort {

	private final RestClient apprentice;

	public ApprenticeHttpAdapter(@Qualifier("apprentice") RestClient apprentice) {
		this.apprentice = apprentice;
	}

	@Override
	public List<ApprenticeResponse> findByEducationLineRef(UUID educationLineRef) {
		return apprentice.get()
            .uri("/internal/apprentices/by-education-line/{id}", educationLineRef)
			.retrieve()
			.body(new ParameterizedTypeReference<List<ApprenticeResponse>>() {});
	}
}
