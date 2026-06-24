package local.sop.sopinfo.message.saga.application.infrastructure.instructor;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.message.saga.application.api.dto.InstructorResponse;
import local.sop.sopinfo.message.saga.application.ports.out.instructor.InstructorPort;

@Component
public class InstructorHttpAdapter implements InstructorPort {

	private final RestClient instructor;

	public InstructorHttpAdapter(@Qualifier("instructor") RestClient instructor) {
		this.instructor = instructor;
	}

	@Override
	public InstructorResponse findById(UUID id) {
		InstructorResponse response = instructor.get()
			.uri("/internal/instructors/{id}", id)
			.retrieve()
			.body(InstructorResponse.class);
		if(response == null) {
			throw new RuntimeException("instructor.empty.response");
		}
		return response;
	}
}
