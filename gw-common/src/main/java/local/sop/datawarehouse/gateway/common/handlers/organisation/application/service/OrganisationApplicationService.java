package local.sop.datawarehouse.gateway.common.handlers.organisation.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import local.sop.datawarehouse.gateway.common.handlers.organisation.api.OrganisationDirectory;
import local.sop.datawarehouse.gateway.common.handlers.organisation.api.dto.response.OrganisationResponse;
import local.sop.datawarehouse.gateway.common.handlers.organisation.application.ports.OrganisationPort;

@Service
public class OrganisationApplicationService implements OrganisationDirectory {

    private final OrganisationPort organisationPort;

    public OrganisationApplicationService(OrganisationPort organisationPort) {
        this.organisationPort = organisationPort;
    }

    @Override
    public List<OrganisationResponse> getAllOrganisations() {
        return organisationPort.getAllOrganisations();
    }

}
