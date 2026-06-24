package local.sop.sopinfo.login.interfaceadapters.persistence.jpa;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import local.sop.sopinfo.login.domain.model.Session;
import local.sop.sopinfo.login.domain.model.valueobjects.LoginId;
import local.sop.sopinfo.login.domain.model.valueobjects.SessionToken;
import local.sop.sopinfo.login.domain.ports.out.SessionRepositoryPort;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

@Repository
public class SessionRepositoryAdapter implements SessionRepositoryPort {
	private static final Logger log = LoggerFactory.getLogger(SessionRepositoryAdapter.class);
	private final SessionMapper mapper;
	private final SessionSpringDataRepository sessionRepo;
	private final LoginSpringDataRepository loginRepo;

	public SessionRepositoryAdapter(SessionMapper mapper, SessionSpringDataRepository sessionRepo, LoginSpringDataRepository loginRepo) {
		this.mapper = mapper;
		this.sessionRepo = sessionRepo;
		this.loginRepo = loginRepo;
	}

	@Override
	public void save(Session session) {
		LoginEntity loginEntity = loginRepo.findById(session.getLogin().getId().value())
			.orElseThrow(() -> new ValidationException("login.not.found",
				Map.of("field", "loginId")));
		SessionEntity entity = mapper.toEntity(session, loginEntity);
		sessionRepo.save(entity);
		log.info("saved session with token prefix {}",
			session.getToken().value().substring(0, 8));
	}

	@Override
	public Optional<Session> findBySessionToken(SessionToken sessionToken) {
		return sessionRepo.findBySessionToken(sessionToken.value()).map(mapper::toDomain);
	}

	@Override
	public boolean deleteBySessionToken(SessionToken sessionToken) {
		int deleted = sessionRepo.deleteBySessionToken(sessionToken.value());
		log.info("deleted session with token prefix {}: {}",
			sessionToken.value().substring(0, 8),
			deleted > 0 ? "success" : "not found");
		return deleted > 0;
	}

	@Override
	public Optional<Session> findActiveByLoginId(LoginId loginId) {
		return sessionRepo.findActiveByLoginId(loginId.value(), LocalDateTime.now())
			.map(mapper::toDomain);
	}

	@Override
	public void deleteByLoginId(LoginId loginId) {
		sessionRepo.deleteByLoginId(loginId.value());
		log.info("deleted all sessions for loginId {}", loginId.value());
	}
}
