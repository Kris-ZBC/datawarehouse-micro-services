package local.sop.sopinfo.registration.saga.application.api.dto.login;

import java.util.UUID;

public record LoginResponse(
    UUID id,
    UUID personRef,
    String username,
    String status
) { }
