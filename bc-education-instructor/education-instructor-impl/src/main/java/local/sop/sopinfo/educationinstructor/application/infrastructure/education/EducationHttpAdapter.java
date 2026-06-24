package local.sop.sopinfo.educationinstructor.application.infrastructure.education;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.educationinstructor.application.infrastructure.ports.out.education.EducationPort;
import local.sop.sopinfo.educationinstructor.application.infrastructure.response.EducationResponse;

@Component("educationPort")
public class EducationHttpAdapter implements EducationPort {

    private final Logger log = LoggerFactory.getLogger(EducationHttpAdapter.class);
    private final RestClient restClient;

    public EducationHttpAdapter(@Qualifier("education") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public boolean exists(UUID id) {
        return findById(id).isPresent();
    }

    @Override
    public Optional<EducationResponse> findById(UUID id) {
        try {
            ResponseEntity<EducationResponse> response = restClient.get()
                    .uri("/internal/educations/{id}", id)
                    .retrieve()
                    .toEntity(EducationResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(response.getBody());
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error occurred while fetching education by ID: {}", id, e);
            return Optional.empty();
        }
    }
}
