package local.sop.datawarehouse.registration.saga.application.api.dto.login;

import java.util.UUID;

public record LoginResponse(
    UUID id,
    UUID personRef,
    String username,
    String status
) { }
