package local.sop.datawarehouse.personnotification.application.infrastructure.notification;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.datawarehouse.personnotification.application.infrastructure.ports.out.notification.NotificationPort;
import local.sop.datawarehouse.personnotification.application.infrastructure.response.NotificationResponse;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component("notificationPort")
public class NotificationHttpAdapter implements NotificationPort {
    
     Logger log = LoggerFactory.getLogger(NotificationHttpAdapter.class);
    private final RestClient restClient;

public NotificationHttpAdapter(@Qualifier("notification") RestClient restClient) {
        this.restClient = restClient;
    }
    @Override
    public boolean exists(UUID id) {
        return findById(id).isPresent();
    }
    
    @Override
    public Optional<NotificationResponse> findById(UUID id) {
        try {
            ResponseEntity<NotificationResponse> response = restClient.get()
                    .uri("/internal/notifications/{id}", id)
                    .retrieve()
                    .toEntity(NotificationResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(response.getBody());
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Failed to find notification by id={}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }      
}

    