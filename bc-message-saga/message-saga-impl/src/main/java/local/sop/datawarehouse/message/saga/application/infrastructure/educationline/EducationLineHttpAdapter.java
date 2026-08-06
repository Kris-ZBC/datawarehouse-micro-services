package local.sop.datawarehouse.message.saga.application.infrastructure.educationline;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.datawarehouse.message.saga.application.api.dto.EducationLineResponse;
import local.sop.datawarehouse.message.saga.application.ports.out.educationline.EducationLinePort;

import org.springframework.core.ParameterizedTypeReference;

@Component
public class EducationLineHttpAdapter implements EducationLinePort {

	private final RestClient educationline;

	public EducationLineHttpAdapter(@Qualifier("educationline") RestClient educationline) {
		this.educationline = educationline;
	}

	@Override
	public List<EducationLineResponse> findByEducationRef(UUID educationRef) {
		return educationline.get()
			.uri("/internal/educationlines/{id}/education-ref", educationRef)
			.retrieve()
			.body(new ParameterizedTypeReference<List<EducationLineResponse>>() {});
	}
}
