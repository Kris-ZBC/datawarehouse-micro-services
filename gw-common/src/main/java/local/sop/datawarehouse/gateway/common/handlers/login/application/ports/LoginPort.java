package local.sop.datawarehouse.gateway.common.handlers.login.application.ports;

import java.time.LocalDateTime;
import java.util.UUID;
 
public interface LoginPort {
    LoginResult login(String username, String password);
    void logout(String sessionToken);
 
    // Local mirror of login-saga's LoginResult — same reasoning used
    // throughout this codebase for every other cross-module DTO.
    record LoginResult(UUID loginId, UUID personRef, String username, String role, String sessionToken,
            LocalDateTime createdAt, LocalDateTime expiresAt) {}
}
