package local.sop.datawarehouse.registration.saga.application.infrastructure.consent;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.datawarehouse.registration.saga.application.api.dto.consent.ConsentResponse;
import local.sop.datawarehouse.registration.saga.application.ports.out.consent.ConsentPort;

@Component
public class ConsentHttpAdapter implements ConsentPort {
    
    private final RestClient consent;

    public ConsentHttpAdapter(@Qualifier("consent") RestClient consent) {
        this.consent = consent;
    }

    @Override
    public ConsentResponse getById(UUID id) {
        return consent.get()
            .uri("/internal/consents/consent/{id}", id)
            .retrieve()
            .body(ConsentResponse.class);
    }
}
