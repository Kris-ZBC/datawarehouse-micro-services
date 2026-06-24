package local.sop.sopinfo.organisation.application.api;
import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.organisation.application.api.dto.OrganisationResponse;

public interface OrganisationDirectory {
    public Optional<OrganisationResponse> findById(UUID id);
}
