package local.sop.datawarehouse.registration.saga.application.api.dto.organization;

import java.util.UUID;

public record OrganisationResponse(UUID id, String name, String cvr) { };

