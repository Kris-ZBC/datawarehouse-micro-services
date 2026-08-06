package local.sop.datawarehouse.education.saga.application.infrastructure.educationinstructor;

import java.util.Map;
import java.util.Optional;

import org.springframework.web.client.RestClient;

import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateUpdate;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.education.saga.application.api.dto.CreateEducationInstructorCmd;
import local.sop.datawarehouse.education.saga.application.api.dto.EducationInstructorResponse;
import local.sop.datawarehouse.education.saga.application.infrastructure.request.PayloadCreateEducationInstructor;
import local.sop.datawarehouse.education.saga.application.ports.out.educationinstructor.EducationInstructorPort;

public class EducationInstructorHttpAdapter implements EducationInstructorPort {
    
    private final RestClient educationInstructor;
    
    public EducationInstructorHttpAdapter(RestClient educationInstructor) {
        this.educationInstructor = educationInstructor;
    }

    @Override
    public EducationInstructorResponse createEducationInstructor(CreateEducationInstructorCmd cmd) {
        EducationInstructorResponse response = educationInstructor.post()
            .uri("/internal/education-instructors/create")
            .body(new PayloadCreateEducationInstructor(cmd.id()))
            .retrieve().body(EducationInstructorResponse.class);
        
        if (response == null || response.id() == null) {
            throw new ConflictException("educationinstructor.creation.failed", Map.of("id", cmd.id().toString()));
        }
        return response;
    }

    @Override
    public Optional<EducationInstructorResponse> findById(CompositeKey id) {
        EducationInstructorResponse response = educationInstructor.get()
            .uri("/internal/education-instructors/{id}", id)
            .retrieve().body(EducationInstructorResponse.class);
            
        if (response == null || response.id() == null) {
            throw new ConflictException("educationinstructor.not.found", Map.of("id", id.toString()));
        }

        return Optional.of(response);
    }

    @Override
    public EducationInstructorResponse deactivateEducationInstructor(CompositeKey id) {
        EducationInstructorResponse response = educationInstructor.put()
            .uri("/internal/education-instructors/{id}/deactivate", id)
            .retrieve().body(EducationInstructorResponse.class);
            
        if (response == null || response.id() == null) {
            throw new ConflictException("educationinstructor.deactivation.failed", Map.of("id", id.toString()));
        }
        return response;
    }

    @Override
    public EducationInstructorResponse activateEducationInstructor(CompositeKey id) {
        EducationInstructorResponse response = educationInstructor.put()
            .uri("/internal/education-instructors/{id}/activate", id)
            .retrieve().body(EducationInstructorResponse.class);
            
        if (response == null || response.id() == null) {
            throw new ConflictException("educationinstructor.activation.failed", Map.of("id", id.toString()));
        }
        return response;
    }

    @Override
    public ResponseCompensated compensateCreateEducationInstructor(CompositeKey id, Class<?> clazz,
            SagaOutcome sagaState) {
        ResponseCompensated response = educationInstructor.post()
            .uri("/education/{educationId}/instructor/{instructorId}/compensate-create", id.key1(), id.key2())
            .body(new PayloadCompensateCreate(clazz, sagaState))
            .retrieve().body(ResponseCompensated.class);
            
        if (response == null) {
            throw new ConflictException("educationinstructor.creation.compensation.failed", Map.of("id", id.toString()));
        }

        return response;
    }

    @Override
    public ResponseCompensated compensateActivateEducationInstructor(CompositeKey id, Class<?> clazz,
            SagaOutcome sagaState) {
        ResponseCompensated response = educationInstructor.post()
            .uri("/education/{educationId}/instructor/{instructorId}/compensate-activate", id.key1(), id.key2())
            .body(new PayloadCompensateUpdate(clazz, sagaState, null, null))
            .retrieve().body(ResponseCompensated.class);
        if (response == null) {
            throw new ConflictException("educationinstructor.activation.compensation.failed", Map.of("id", id.toString()));
        }
        return response;
    }

    @Override
    public ResponseCompensated compensateDeactivateEducationInstructor(CompositeKey id, Class<?> clazz,
            SagaOutcome sagaState) {
        ResponseCompensated response = educationInstructor.post()
            .uri("/education/{educationId}/instructor/{instructorId}/compensate-deactivate", id.key1(), id.key2())
            .body(new PayloadCompensateUpdate(clazz, sagaState, null, null))
            .retrieve().body(ResponseCompensated.class);
        if (response == null) {
            throw new ConflictException("educationinstructor.deactivation.compensation.failed", Map.of("id", id.toString()));
        }
        return response;
    }
}
