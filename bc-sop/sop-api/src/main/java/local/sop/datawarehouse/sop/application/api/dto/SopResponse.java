package local.sop.datawarehouse.sop.application.api.dto;

import java.util.UUID;

public record SopResponse(
    UUID id,
    String name,
    String address,
    String education
) {}
