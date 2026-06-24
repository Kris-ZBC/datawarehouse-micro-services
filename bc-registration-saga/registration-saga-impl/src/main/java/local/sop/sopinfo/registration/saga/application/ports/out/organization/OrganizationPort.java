package local.sop.sopinfo.registration.saga.application.ports.out.organization;

import java.util.UUID;

import local.sop.sopinfo.registration.saga.application.api.dto.organization.OrganisationResponse;

public interface OrganizationPort {
	OrganisationResponse getById(UUID id);
}
