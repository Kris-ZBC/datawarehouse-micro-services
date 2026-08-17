package local.sop.datawarehouse.login.application.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record LoginResult(UUID loginId, UUID personRef, String username, String sessionToken, LocalDateTime createdAt, LocalDateTime expiresAt) {

}
