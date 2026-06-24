package local.sop.sopinfo.login.domain.ports.out;

import java.util.Optional;

import local.sop.sopinfo.login.domain.model.Session;
import local.sop.sopinfo.login.domain.model.valueobjects.LoginId;
import local.sop.sopinfo.login.domain.model.valueobjects.SessionToken;

public interface SessionRepositoryPort {
    void save(Session session);
    Optional<Session> findBySessionToken(SessionToken sessionToken);
    Optional<Session> findActiveByLoginId(LoginId loginId);  // ← new
    boolean deleteBySessionToken(SessionToken sessionToken);
    void deleteByLoginId(LoginId loginId); 
}
