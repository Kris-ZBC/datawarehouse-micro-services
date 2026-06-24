package local.sop.sopinfo.education.saga.application.infrastructure.education;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.education.saga.application.api.dto.CreateEducationCmd;
import local.sop.sopinfo.education.saga.application.api.dto.EducationResponse;
import local.sop.sopinfo.education.saga.application.infrastructure.request.PayloadCreateCompensate;
import local.sop.sopinfo.education.saga.application.infrastructure.request.PayloadUpdateCompensate;
import local.sop.sopinfo.education.saga.application.infrastructure.request.PayloadEducationCreate;
import local.sop.sopinfo.education.saga.application.ports.out.education.EducationPort;
import local.sop.sopinfo.sharedkernel.exceptions.ConflictException;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;

public class EducationHttpAdapter implements EducationPort {

    private final RestClient education;

    public EducationHttpAdapter(@Qualifier("education") RestClient education) {
        this.education = education;
    }
    
    @Override
    public UUID createEducation(CreateEducationCmd cmd) {
        EducationResponse response = education.post()
            .uri("/internal/educations/create")
            .body(new PayloadEducationCreate(cmd.name(), cmd.category()))
            .retrieve().body(EducationResponse.class);
            
        if (response == null || response.id() == null) {
            throw new ConflictException("education.creation.failed", Map.of("educationName", cmd.name()));
        }

        return response.id();
    }

    @Override
    public EducationResponse findEducationById(UUID educationId) {
        EducationResponse response = education.get()
            .uri("/internal/educations/{id}", educationId)
            .retrieve().body(EducationResponse.class);
        
        if (response == null || response.id() == null) {
            throw new ConflictException("education.not.found", Map.of("educationId", educationId.toString()));
        }

        return response;
    }

    @Override
    public EducationResponse updateEducationName(UUID educationId, String name) {
        EducationResponse response = education.put()
            .uri("/internal/educations/{id}/name", educationId)
            .body(name)
            .retrieve().body(EducationResponse.class);

        if (response == null || response.id() == null) {
            throw new ConflictException("education.update.name.failed", Map.of("educationId", educationId.toString()));
        }
        
        return response;
    }

    @Override
    public EducationResponse updateEducationCategory(UUID educationId, String category) {
        EducationResponse response = education.put()
            .uri("/internal/educations/{id}/category", educationId)
            .body(category)
            .retrieve().body(EducationResponse.class);

        if (response == null || response.id() == null) {
            throw new ConflictException("education.update.category.failed", Map.of("educationId", educationId.toString()));
        }
        
        return response;
    }

    @Override
    public EducationResponse activateEducation(UUID educationId) {
        EducationResponse response = education.put()
            .uri("/internal/educations/{id}/activate", educationId)
            .retrieve().body(EducationResponse.class);

        if (response == null || response.id() == null) {
            throw new ConflictException("education.activate.failed", Map.of("educationId", educationId.toString()));
        }

        return response;
    }

    @Override
    public EducationResponse deactivateEducation(UUID educationId) {
        EducationResponse response = education.put()
            .uri("/internal/educations/{id}/deactivate", educationId)
            .retrieve().body(EducationResponse.class);

        if (response == null || response.id() == null) {
            throw new ConflictException("education.deactivate.failed", Map.of("educationId", educationId.toString()));
        }

        return response;
    }

    @Override
    public ResponseCompensated compensateCreateEducation(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        ResponseCompensated response = education.post()
            .uri("/internal/educations/compensatecreate/{id}", id)
            .body(new PayloadCreateCompensate(id, clazz, sagaState))
            .retrieve().body(ResponseCompensated.class);
            
            
        if (response == null) {
            throw new ConflictException("education.create.compensation.failed", Map.of("id", id.toString()));
        }
        
        return response;
    }

    @Override
    public ResponseCompensated compensateUpdateEducationName(UUID id, Class<?> clazz, SagaOutcome sagaState, String previousName) {
        ResponseCompensated response = education.post()
            .uri("/internal/educations/compensateupdatename/{id}", id)
            .body(new PayloadUpdateCompensate(id, clazz, sagaState, previousName, null))
            .retrieve().body(ResponseCompensated.class);
            
        if (response == null) {
            throw new ConflictException("education.update.name.compensation.failed", Map.of("id", id.toString()));
        }
        
        return response;
    }

    @Override
    public ResponseCompensated compensateUpdateEducationCategory(UUID id, Class<?> clazz, SagaOutcome sagaState, String previousCategory) {
        ResponseCompensated response = education.post()
            .uri("/internal/educations/compensateupdatecategory/{id}", id)
            .body(new PayloadUpdateCompensate(id, clazz, sagaState, previousCategory, null))
            .retrieve().body(ResponseCompensated.class);
            
        if (response == null) {
            throw new ConflictException("education.update.category.compensation.failed", Map.of("id", id.toString()));
        }
        
        return response;
    }

    @Override
    public ResponseCompensated compensateActivateEducation(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        ResponseCompensated response = education.post()
            .uri("/internal/educations/compensateactivate/{id}", id)
            .body(new PayloadUpdateCompensate(id, clazz, sagaState, null, null))
            .retrieve().body(ResponseCompensated.class);
            
            
        if (response == null) {
            throw new ConflictException("education.activate.compensation.failed", Map.of("id", id.toString()));
        }
        
        return response;    }

    @Override
    public ResponseCompensated compensateDeactivateEducation(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        ResponseCompensated response = education.post()
            .uri("/internal/educations/compensatedeactivate/{id}", id)
            .body(new PayloadUpdateCompensate(id, clazz, sagaState, null, null))
            .retrieve().body(ResponseCompensated.class);
            
            
        if (response == null) {
            throw new ConflictException("education.deactivate.compensation.failed", Map.of("id", id.toString()));
        }
        
        return response;
    }
}
