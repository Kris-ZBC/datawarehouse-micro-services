package local.sop.datawarehouse.messageperson.application.infrastructure.message;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.datawarehouse.messageperson.application.infrastructure.ports.out.message.MessagePort;
import local.sop.datawarehouse.messageperson.application.infrastructure.response.MessageResponse;

@Component("messagePort")
public class MessageHttpAdapter implements MessagePort {

    Logger log = LoggerFactory.getLogger(MessageHttpAdapter.class);
    private RestClient restClient;

    public MessageHttpAdapter(@Qualifier("message") RestClient restClient) {
        this.restClient = restClient;
    }
    
    @Override
    public Optional<MessageResponse> findById(UUID id) {
        // Implements for finding message by ID
        try {
            ResponseEntity<MessageResponse> response = restClient.get()
                    .uri("/internal/messages/{id}", id)
                    .retrieve()
                    .toEntity(MessageResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(response.getBody());
            }
            return Optional.empty();
        } catch (Exception e) {
            // Handle exceptions (e.g., log the error)
            log.error("Failed to fetch message by ID: {}", id, e.getMessage());
            return Optional.empty();
        }

    }

    @Override
    public boolean exists(UUID id) {
        return findById(id).isPresent();
    }


}
