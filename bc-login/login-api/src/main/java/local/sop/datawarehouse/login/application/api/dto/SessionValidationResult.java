package local.sop.datawarehouse.login.application.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;


// CHANGED: role is now a plain String — see CreateSessionCmd for why.
public record SessionValidationResult(
    boolean valid,
    UUID loginId,
    UUID personRef,
    String username,
    String role,
    LocalDateTime expiresAt
) {
    public static SessionValidationResult invalid() {
        return new SessionValidationResult(false, null, null, null, null, null);
    }
}
