package local.sop.datawarehouse.login.application.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionValidationResult(
    boolean valid,
    UUID loginId,
    UUID personRef,
    String username,
    LocalDateTime expiresAt
) {
    public static SessionValidationResult invalid() {
        return new SessionValidationResult(false, null, null, null, null);
    }
}
