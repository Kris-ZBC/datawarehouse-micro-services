package local.sop.datawarehouse.login.saga.application.ports.out.login;

import java.util.UUID;
 
import local.sop.datawarehouse.login.saga.application.api.dto.LoginResult;
import local.sop.datawarehouse.sharedlib.enums.UserRole;
 
/**
 * CHANGED: was login(username, password) as one call plus a generic
 * compensate(id, clazz, sagaState) targeting the Login aggregate.
 *
 * Split to match bc-login's own authenticate()/createSession() split.
 * authenticate() creates nothing (pure credential check), so there's
 * nothing here for the saga to compensate if a later step (role
 * resolution, consent check) fails after it succeeds — that's exactly
 * the semantic problem the old generic compensate() had: it undid the
 * Login even though this saga never created it (registration-saga
 * did). Only createSession() creates a resource (the Session) — and
 * the correct way to undo a Session is to log it out, which already
 * exists as its own operation. So compensate(id, clazz, sagaState) is
 * gone entirely; logout(sessionToken) replaces it as the one
 * compensating action this saga ever needs.
 */
public interface LoginPort {
	AuthenticationResult authenticate(String username, String password);
	LoginResult createSession(UUID loginId, UserRole role);
	void logout(String sessionToken);
 
	// Local mirror of bc-login's AuthenticationResult — same reasoning
	// as every other cross-BC DTO in this codebase: this saga depends
	// on a contract, not on bc-login's internal module structure.
	record AuthenticationResult(UUID loginId, UUID personRef, String username) {}
}
