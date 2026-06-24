package local.sop.sopinfo.registration.saga.application.infrastructure.instructor;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.registration.saga.application.api.dto.instructor.CreateInstructorCmd;
import local.sop.sopinfo.registration.saga.application.api.dto.instructor.InstructorResponse;
import local.sop.sopinfo.registration.saga.application.ports.out.instructor.InstructorPort;

import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;

@Component
public class InstructorHttpAdapter implements InstructorPort {

	private final RestClient instructor;

	public InstructorHttpAdapter(@Qualifier("instructor") RestClient instructor) {
		this.instructor = instructor;
	}

	@Override
    public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        return instructor.put()
            .uri("/internal/instructors/{id}/compensate/create", id)
            .body(new PayloadCompensateCreate(clazz, sagaState))
            .retrieve()
            .body(ResponseCompensated.class);
    }

    @Override
    public UUID create(CreateInstructorCmd cmd) {
        return instructor.post()
            .uri("/internal/instructors")
            .body(cmd)
            .retrieve()
            .body(UUID.class);
    }

    @Override
    public InstructorResponse getById(UUID id) {
        return instructor.get()
            .uri("/internal/instructors/{id}", id)
            .retrieve()
            .body(InstructorResponse.class);
    }
}