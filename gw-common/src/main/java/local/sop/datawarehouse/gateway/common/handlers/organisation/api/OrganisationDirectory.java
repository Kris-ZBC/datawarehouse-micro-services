package local.sop.datawarehouse.gateway.common.handlers.organisation.api;

import java.util.List;

import local.sop.datawarehouse.gateway.common.handlers.organisation.api.dto.response.OrganisationResponse;

public interface OrganisationDirectory {
    List<OrganisationResponse> getAllOrganisations();
}
