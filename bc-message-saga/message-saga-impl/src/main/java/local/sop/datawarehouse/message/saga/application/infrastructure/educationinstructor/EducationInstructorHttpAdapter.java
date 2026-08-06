package local.sop.datawarehouse.message.saga.application.infrastructure.educationinstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.datawarehouse.message.saga.application.api.dto.EducationInstructorResponse;
import local.sop.datawarehouse.message.saga.application.ports.out.educationinstructor.EducationInstructorPort;

@Component
public class EducationInstructorHttpAdapter implements EducationInstructorPort {

	private final RestClient educationinstructor;

	public EducationInstructorHttpAdapter(@Qualifier("educationinstructor") RestClient educationinstructor) {
		this.educationinstructor = educationinstructor;
	}

	@Override
	public List<EducationInstructorResponse> findByEducationRef(UUID educationRef) {
		return educationinstructor.get()
			.uri("/internal/educationinstructors/by-education-ref/{id}", educationRef)
			.retrieve()
			.body(new ParameterizedTypeReference<List<EducationInstructorResponse>>() {});
	}
}
