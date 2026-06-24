package local.sop.sopinfo.sopinstructor.application.infrastructure.instructor;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.sopinstructor.application.infrastructure.ports.out.instructor.InstructorPort;
import local.sop.sopinfo.sopinstructor.application.infrastructure.response.InstructorResponse;

@Component("instructorPort")
public class InstructorHttpAdapter implements InstructorPort {

    Logger log = LoggerFactory.getLogger(InstructorHttpAdapter.class);
    private RestClient restClient;

    public InstructorHttpAdapter(@Qualifier("instructor") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public boolean exists(UUID id) {
        return findById(id).isPresent();
    }
    
    @Override
    public Optional<InstructorResponse> findById(UUID id) {
        // Implements for finding instructor line by ID
        try {
            ResponseEntity<InstructorResponse> response = restClient.get()
                    .uri("/internal/instructors/{id}", id)
                    .retrieve()
                    .toEntity(InstructorResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(response.getBody());
            }
            return Optional.empty();
        } catch (Exception e) {
            // Handle exceptions (e.g., log the error)
            log.error("Error occurred while fetching instructor by ID: {}", id, e);
            return Optional.empty();
        }

    }

}
