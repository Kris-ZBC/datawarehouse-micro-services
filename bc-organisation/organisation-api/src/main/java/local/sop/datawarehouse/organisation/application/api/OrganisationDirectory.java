package local.sop.datawarehouse.organisation.application.api;
import java.util.Optional;
import java.util.UUID;

import local.sop.datawarehouse.organisation.application.api.dto.OrganisationResponse;

public interface OrganisationDirectory {
    public Optional<OrganisationResponse> findById(UUID id);
}
