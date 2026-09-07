package local.sop.datawarehouse.gateway.common.handlers.login.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;
 
// No sessionToken here — it's set as an HttpOnly cookie, never exposed
// to JS. This is only what the frontend needs for UI/authorization state.
public record LoginResponse(UUID loginId, UUID personRef, String username, String role, LocalDateTime expiresAt) {}
