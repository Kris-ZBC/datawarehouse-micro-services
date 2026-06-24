package local.sop.sopinfo.login.saga.application.service;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import local.sop.sopinfo.login.saga.application.api.LoginSagaDirectory;
import local.sop.sopinfo.login.saga.application.api.dto.LoginCmd;
import local.sop.sopinfo.login.saga.application.api.dto.LoginResult;
import local.sop.sopinfo.login.saga.application.infrastructure.response.ResponseCompensated;
import local.sop.sopinfo.login.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.sopinfo.login.saga.application.ports.out.consent.ConsentPort;
import local.sop.sopinfo.login.saga.application.ports.out.login.LoginPort;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.exceptions.ConflictException;
import local.sop.sopinfo.sharedkernel.exceptions.DomainException;


@Service
public class LoginSagaApplicationService implements LoginSagaDirectory {
	private static final Logger log = LoggerFactory.getLogger(LoginSagaApplicationService.class);

	private final LoginPort logins;
	private final ConsentPort consents;
	private final AuditlogPort auditlogs;

	public LoginSagaApplicationService(LoginPort logins, ConsentPort consents, AuditlogPort auditlogs) {
		this.logins = logins;
		this.consents = consents;
		this.auditlogs = auditlogs;
	}

	@Override
	public LoginResult login(LoginCmd cmd) {
		LoginResult loginResult;
		try {
			loginResult = logins.login(cmd.username(), cmd.password());
		} catch (DomainException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			log.warn("SAGA: login failed for user {}", cmd.username());
			throw new ConflictException("login.notcreated", Map.of("object", "login"));
		}
		UUID loginId = loginResult.loginId();
		UUID personRef = loginResult.personRef();

		try {
			boolean hasConsent = consents.hasConsent(personRef);
			if (!hasConsent) {
				log.warn("SAGA: user {} has not given consent, compensating login {}", personRef, loginId);
				ResponseCompensated loginCompensationResult = logins.compensate(loginId, getClass(), SagaOutcome.COMPENSATE);
				log.warn("SAGA: login compensated {}. Success: {}", loginId, loginCompensationResult.success());
				throw new ConflictException("consent.notgiven", Map.of("object", "consent"));
			}
		} catch (DomainException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			log.warn("SAGA: consent check failed for user {}, compensating login {}", personRef, loginId);
			ResponseCompensated loginCompensationResult = logins.compensate(loginId, getClass(), SagaOutcome.COMPENSATE);
			log.warn("SAGA: login compensated {}. Success: {}", loginId, loginCompensationResult.success());
			throw new ConflictException("consent.check.failed", Map.of("object", "consent"));
		}

		try {
			auditlogs.create(cmd.actorRef(), cmd.actorType(), cmd.severity(), cmd.originSystem(), cmd.originService(),
				cmd.originComponent(), cmd.data(), cmd.description());
		} catch (DomainException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			log.warn("SAGA: auditlog create failed, compensating. LoginId: {}", loginId);
			ResponseCompensated loginCompensationResult = logins.compensate(loginId, getClass(), SagaOutcome.COMPENSATE);
			log.warn("SAGA: login compensated {}. Success: {}", loginId, loginCompensationResult.success());
			throw new ConflictException("auditlog.notcreated", Map.of("object", "auditlog"));
		}

		return loginResult;
	}
}
