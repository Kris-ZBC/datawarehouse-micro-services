package local.sop.datawarehouse.organisation.application.api.dto;

import java.util.UUID;

public record OrganisationResponse(UUID id, String name, String cvr) { };

