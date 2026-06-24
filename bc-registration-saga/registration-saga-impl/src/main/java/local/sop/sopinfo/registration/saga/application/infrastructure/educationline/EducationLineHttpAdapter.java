package local.sop.sopinfo.registration.saga.application.infrastructure.educationline;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.registration.saga.application.api.dto.educationline.EducationLineResponse;
import local.sop.sopinfo.registration.saga.application.ports.out.educationline.EducationLinePort;

@Component
public class EducationLineHttpAdapter implements EducationLinePort {

	private final RestClient educationline;

	public EducationLineHttpAdapter(@Qualifier("educationline") RestClient educationline) {
		this.educationline = educationline;
	}

    @Override
    public EducationLineResponse getById(UUID id) {
        return educationline.get()
            .uri("/internal/educationlines/{id}", id)
            .retrieve()
            .body(EducationLineResponse.class);
    }
}