package local.sop.datawarehouse.gateway.common.handlers.organisation.application.ports;

import java.util.List;

import local.sop.datawarehouse.gateway.common.handlers.organisation.api.dto.response.OrganisationResponse;

public interface OrganisationPort {

    List<OrganisationResponse> getAllOrganisations();
}
