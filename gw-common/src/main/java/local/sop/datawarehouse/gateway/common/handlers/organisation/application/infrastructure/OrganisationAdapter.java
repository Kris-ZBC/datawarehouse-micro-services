package local.sop.datawarehouse.gateway.common.handlers.organisation.application.infrastructure;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.gateway.common.handlers.organisation.api.dto.response.OrganisationResponse;
import local.sop.datawarehouse.gateway.common.handlers.organisation.application.ports.OrganisationPort;

@Component
public class OrganisationAdapter implements OrganisationPort {

    private final RestClient restClient;

    public OrganisationAdapter(@Qualifier("organisation") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<OrganisationResponse> getAllOrganisations() {
        return restClient.get()
                .uri("/internal/organisations")
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new ValidationException(
                            "sopId", Map.of("error", response.getStatusCode().toString())); 
                })
                .body(new ParameterizedTypeReference<List<OrganisationResponse>>() {});
    }


}
