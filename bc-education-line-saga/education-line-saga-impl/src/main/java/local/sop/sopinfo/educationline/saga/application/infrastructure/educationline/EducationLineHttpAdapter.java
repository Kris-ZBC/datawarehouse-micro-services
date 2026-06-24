package local.sop.sopinfo.educationline.saga.application.infrastructure.educationline;

import java.util.UUID;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.educationline.saga.application.api.dto.EducationLineResponse;
import local.sop.sopinfo.educationline.saga.application.infrastructure.request.PayloadCompensate;
import local.sop.sopinfo.educationline.saga.application.infrastructure.request.PayloadCompensateName;
import local.sop.sopinfo.educationline.saga.application.infrastructure.request.PayloadCompensateDuration;
import local.sop.sopinfo.educationline.saga.application.infrastructure.request.PayloadEducationLineCreate;
import local.sop.sopinfo.educationline.saga.application.infrastructure.request.PayloadEducationLineDurationUpdate;
import local.sop.sopinfo.educationline.saga.application.infrastructure.request.PayloadEducationLineNameUpdate;
import local.sop.sopinfo.educationline.saga.application.infrastructure.response.ResponseCompensated;
import local.sop.sopinfo.educationline.saga.application.ports.out.educationline.EducationLinePort;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.exceptions.ConflictException;

@Component
public class EducationLineHttpAdapter implements EducationLinePort {
    private final RestClient educationLine;
    private static final String BASE_URL = "/internal/educationlines";
    
    public EducationLineHttpAdapter(@Qualifier("education-line") RestClient educationLine) {
        this.educationLine = educationLine;
    }

    @Override
    public UUID createEducationLine(String name, int durationYears, int durationMonths, int durationDays, UUID educationRef) {
        EducationLineResponse response = educationLine.post()
            .uri(BASE_URL + "/create")
            .body(new PayloadEducationLineCreate(name, durationYears, durationMonths, durationDays, educationRef))
            .retrieve().body(EducationLineResponse.class);

        return validateResponse(response);
    }
    
    @Override
    public UUID updateEducationLineName(UUID id, String name) {
        EducationLineResponse response = educationLine.put()
            .uri(BASE_URL + "/{id}/name", id)
            .body(new PayloadEducationLineNameUpdate(name))
            .retrieve().body(EducationLineResponse.class);
        
        return validateResponse(response);
    }

    @Override
    public EducationLineResponse findEducationLineById(UUID id) {
        EducationLineResponse response = educationLine.get()
            .uri(BASE_URL + "/{id}", id)
            .retrieve().body(EducationLineResponse.class);
        
        if (response == null) {
            throw new ConflictException("educationline.not.found", Map.of("id", id.toString()));
        }
        
        return response;
    }

    @Override
    public UUID updateEducationLineDuration(UUID id, int durationYears, int durationMonths, int durationDays) {
        EducationLineResponse response = educationLine.put()
            .uri(BASE_URL + "/{id}/duration", id)
            .body(new PayloadEducationLineDurationUpdate(durationYears, durationMonths, durationDays))
            .retrieve().body(EducationLineResponse.class);

        return validateResponse(response);
    }
	
    @Override
    public UUID deactivateEducationLine(UUID id) {
        EducationLineResponse response = educationLine.put()
            .uri(BASE_URL + "/{id}/deactivate", id)
            .retrieve().body(EducationLineResponse.class);

        return validateResponse(response);
    }

    @Override
    public UUID activateEducationLine(UUID id) {
        EducationLineResponse response = educationLine.put()
            .uri(BASE_URL + "/{id}/activate", id)
            .retrieve().body(EducationLineResponse.class);

        return validateResponse(response);
    }

    @Override
    public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        ResponseCompensated response = educationLine.post()
            .uri(BASE_URL + "/{id}/compensate", id)
            .body(new PayloadCompensate(id, clazz, sagaState))
            .retrieve().body(ResponseCompensated.class);
        
			if (response == null) {
				throw new ConflictException("educationline.compensation.failed", Map.of("id", id.toString()));
        }
        
        return response;
    }

	@Override
	public ResponseCompensated compensateUpdateName(UUID id, Class<?> clazz, SagaOutcome sagaState, String previousName) {
		ResponseCompensated response = educationLine.post()
			.uri(BASE_URL + "/{id}/compensatename", id)
			.body(new PayloadCompensateName(id, clazz, sagaState, previousName))
			.retrieve()
			.body(ResponseCompensated.class);

			if (response == null) {
			throw new ConflictException("educationline.compensation.failed", Map.of("id", id.toString()));
		}

		return response;
	}

	@Override
	public ResponseCompensated compensateUpdateDuration(UUID id, Class<?> clazz, SagaOutcome sagaState, int previousDurationYears, int previousDurationMonths, int previousDurationDays) {
		ResponseCompensated response = educationLine.post()
			.uri(BASE_URL + "/{id}/compensateduration", id)
			.body(new PayloadCompensateDuration(id, clazz, sagaState, previousDurationYears, previousDurationMonths, previousDurationDays))
			.retrieve()
			.body(ResponseCompensated.class);

		if (response == null) {
			throw new ConflictException("educationline.compensation.failed", Map.of("id", id.toString()));
		}

		return response;
	}

	@Override
	public ResponseCompensated compensateActivate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
		ResponseCompensated response = educationLine.post()
		.uri(BASE_URL + "/{id}/compensateactivate", id)
			.body(new PayloadCompensate(id, clazz, sagaState))
			.retrieve()
			.body(ResponseCompensated.class);
			
		if (response == null) {
			throw new ConflictException("educationline.compensation.failed", Map.of("id", id.toString()));
		}

		return response;
	}

	@Override
	public ResponseCompensated compensateDeactivate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
		ResponseCompensated response = educationLine.post()
			.uri(BASE_URL + "/{id}/compensatedeactivate", id)
			.body(new PayloadCompensate(id, clazz, sagaState))
			.retrieve()
			.body(ResponseCompensated.class);

		if (response == null) {
			throw new ConflictException("educationline.compensation.failed", Map.of("id", id.toString()));
		}

		return response;
	}

    private UUID validateResponse(EducationLineResponse response) {
        if (response == null || response.id() == null) {
            throw new ConflictException("educationline.failed.create", Map.of("", ""));
        }

        return response.id();
    }
}
