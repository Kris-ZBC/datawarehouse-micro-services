package local.sop.sopinfo.registration.saga.application.infrastructure.organization;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.registration.saga.application.api.dto.organization.OrganisationResponse;
import local.sop.sopinfo.registration.saga.application.ports.out.organization.OrganizationPort;
@Component
public class OrganizationHttpAdapter implements OrganizationPort {

	private final RestClient organization;

	public OrganizationHttpAdapter(@Qualifier("organization") RestClient organization) {
		this.organization = organization;
	}

    @Override
    public OrganisationResponse getById(UUID id) {
        return organization.get()
            .uri("/internal/organizations?id={id}", id)
            .retrieve()
            .body(OrganisationResponse.class);
    }
}