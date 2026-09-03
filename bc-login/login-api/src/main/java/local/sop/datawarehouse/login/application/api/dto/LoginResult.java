package local.sop.datawarehouse.login.application.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;


// CHANGED: role is now a plain String, matching bc-login's own
// LoginResult — same reasoning (ApplicationLayerCleanTest's rule: DTO
// records in this package tree shouldn't depend on shared-lib types).
// No equivalent test exists in THIS module yet, but it's the identical
// category of problem, so fixed proactively rather than waiting for a
// second failure of the same kind. LoginPort.createSession() and
// PayloadCreateSession both stay UserRole-typed deliberately — neither
// is a DTO record subject to this rule, and Jackson serializes an enum
// to its .name() by default anyway, producing identical JSON to a
// String field either way.
public record LoginResult(UUID loginId, UUID personRef, String username, String role, String sessionToken, LocalDateTime createdAt, LocalDateTime expiresAt) {

}
