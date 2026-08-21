package local.sop.datawarehouse.gateway.common.handlers.sop.application.infrastructure;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.gateway.common.handlers.sop.api.dto.response.SopResponse;
import local.sop.datawarehouse.gateway.common.handlers.sop.application.ports.SopPort;

@Component
public class SopAdapter implements SopPort {

    private final RestClient restClient;

    public SopAdapter(@Qualifier("sop") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<SopResponse> getAllSops() {
    return restClient.get()
            .uri("/internal/sops")
            .retrieve()
            .onStatus(HttpStatusCode::isError, (request, response) -> {
                throw new ValidationException(
                        "sopId", Map.of("error", response.getStatusCode().toString())); 
            })
            .body(new ParameterizedTypeReference<List<SopResponse>>() {});
    }


}
