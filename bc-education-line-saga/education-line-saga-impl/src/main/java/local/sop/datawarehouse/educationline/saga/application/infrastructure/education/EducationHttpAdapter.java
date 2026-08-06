package local.sop.datawarehouse.educationline.saga.application.infrastructure.education;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.datawarehouse.educationline.saga.application.api.dto.EducationResponse;
import local.sop.datawarehouse.educationline.saga.application.ports.out.education.EducationPort;

@Component
public class EducationHttpAdapter implements EducationPort {
    private final RestClient education;
    private static final String BASE_URL = "/internal/educations";

    public EducationHttpAdapter(@Qualifier("education") RestClient education) {
        this.education = education;
    }

    @Override
    public EducationResponse existsById(UUID id) {
        EducationResponse response = education.get()
            .uri(BASE_URL + "/{id}", id)
            .retrieve().body(EducationResponse.class);
        return response;
    }
}
