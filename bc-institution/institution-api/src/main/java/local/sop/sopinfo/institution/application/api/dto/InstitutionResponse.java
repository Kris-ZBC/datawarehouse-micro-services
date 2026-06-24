package local.sop.sopinfo.institution.application.api.dto;

import java.util.UUID;

public record InstitutionResponse(UUID id, String name, String address) { }
