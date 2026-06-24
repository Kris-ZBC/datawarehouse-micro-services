package local.sop.sopinfo.sopinstructor.application.infrastructure.sop;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.sopinstructor.application.infrastructure.ports.out.sop.SopPort;
import local.sop.sopinfo.sopinstructor.application.infrastructure.response.SopResponse;

@Component("sopPort")
public class SopHttpAdapter implements SopPort {

    Logger log = LoggerFactory.getLogger(SopHttpAdapter.class);
    private RestClient restClient;

    public SopHttpAdapter(@Qualifier("sop") RestClient restClient) {
        this.restClient = restClient;
    }
    
    @Override
    public Optional<SopResponse> findById(UUID id) {
        // Implements for finding sop by ID
        try {
            ResponseEntity<SopResponse> response = restClient.get()
                    .uri("/internal/sops/{id}", id)
                    .retrieve()
                    .toEntity(SopResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(response.getBody());
            }
            return Optional.empty();
        } catch (Exception e) {
            // Handle exceptions (e.g., log the error)
            log.error("Failed to fetch sop by ID: {}", id, e);
            return Optional.empty();
        }

    }

    @Override
    public boolean exists(UUID id) {
        return findById(id).isPresent();
    }


}
