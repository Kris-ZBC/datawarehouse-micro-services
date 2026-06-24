package local.sop.sopinfo.personnotification.application.infrastructure.person;



import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import local.sop.sopinfo.personnotification.application.infrastructure.ports.out.person.PersonPort;
import local.sop.sopinfo.personnotification.application.infrastructure.response.PersonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component("personPort")
public class PersonHttpAdapter implements PersonPort {

    Logger log = LoggerFactory.getLogger(PersonHttpAdapter.class);
    private final RestClient restClient;
    


public PersonHttpAdapter(@Qualifier("person") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public boolean exists(UUID id) {
        return findById(id).isPresent();
    }

    @Override
    public Optional<PersonResponse> findById(UUID id) {
        try {
            ResponseEntity<PersonResponse> response = restClient.get()
                    .uri("/internal/persons/{id}", id)
                    .retrieve()
                    .toEntity(PersonResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(response.getBody());
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Failed to find person by id={}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

}

