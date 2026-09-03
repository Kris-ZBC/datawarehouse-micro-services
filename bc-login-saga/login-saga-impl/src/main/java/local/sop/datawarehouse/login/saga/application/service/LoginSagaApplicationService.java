package local.sop.datawarehouse.login.saga.application.service;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import local.sop.datawarehouse.login.saga.application.api.LoginSagaDirectory;
import local.sop.datawarehouse.login.saga.application.api.dto.LoginCmd;
import local.sop.datawarehouse.login.saga.application.api.dto.LoginResult;
import local.sop.datawarehouse.login.saga.application.ports.out.apprentice.ApprenticePort;
import local.sop.datawarehouse.login.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.datawarehouse.login.saga.application.ports.out.consent.ConsentPort;
import local.sop.datawarehouse.login.saga.application.ports.out.instructor.InstructorPort;
import local.sop.datawarehouse.login.saga.application.ports.out.login.LoginPort;
import local.sop.datawarehouse.login.saga.application.ports.out.login.LoginPort.AuthenticationResult;
import local.sop.datawarehouse.sharedlib.enums.UserRole;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.DomainException;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;


/**
 * CHANGED: reshaped around the authenticate()/createSession() split —
 * see LoginPort's Javadoc for why. New step: role resolution, checking
 * bc-instructor then bc-apprentice. Per explicit instruction, there is
 * NO fallback role for someone found in neither — every Login today
 * gets created BY registration-saga, which always creates an
 * Instructor or Apprentice alongside it, so "found in neither" is a
 * data-integrity problem, not a normal case, and fails loudly
 * (NotFoundException) rather than silently defaulting to a role.
 *
 * Compensation is also corrected as a direct consequence of the split:
 * authenticate(), role resolution, and the consent check all create
 * nothing, so a failure at any of those points has nothing to
 * compensate — unlike the OLD code, which compensated the Login
 * (something this saga never created) on every failure path. Only
 * createSession() creates a resource (the Session), so only a failure
 * AFTER it succeeds (i.e. the audit log step) needs cleanup — and that
 * cleanup is logout(sessionToken), not a generic compensate() call.
 */
@Service
public class LoginSagaApplicationService implements LoginSagaDirectory {
	private static final Logger log = LoggerFactory.getLogger(LoginSagaApplicationService.class);

	private final LoginPort logins;
	private final InstructorPort instructors;
	private final ApprenticePort apprentices;
	private final ConsentPort consents;
	private final AuditlogPort auditlogs;

	public LoginSagaApplicationService(LoginPort logins, InstructorPort instructors, ApprenticePort apprentices,
			ConsentPort consents, AuditlogPort auditlogs) {
		this.logins = logins;
		this.instructors = instructors;
		this.apprentices = apprentices;
		this.consents = consents;
		this.auditlogs = auditlogs;
	}

	@Override
	public LoginResult login(LoginCmd cmd) {

		// Step 1: authenticate. Creates nothing — a failure here has
		// nothing to compensate.
		AuthenticationResult authResult;
		try {
			authResult = logins.authenticate(cmd.username(), cmd.password());
		} catch (DomainException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			log.warn("SAGA: authentication failed for user {}", cmd.username());
			throw new ConflictException("login.notcreated", Map.of("object", "login"));
		}

		UUID loginId = authResult.loginId();
		UUID personRef = authResult.personRef();

		// Step 2: resolve role. Also creates nothing. Fails loudly —
		// no TECHUSER (or any other) fallback — if the person matches
		// neither bc-instructor nor bc-apprentice.
		UserRole role = resolveRole(personRef);

		// Step 3: consent check. Still nothing created yet.
		try {
			boolean hasConsent = consents.hasConsent(personRef);
			if (!hasConsent) {
				log.warn("SAGA: user {} has not given consent", personRef);
				throw new ConflictException("consent.notgiven", Map.of("object", "consent"));
			}
		} catch (DomainException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			log.warn("SAGA: consent check failed for user {}", personRef);
			throw new ConflictException("consent.check.failed", Map.of("object", "consent"));
		}

		// Step 4: create the session. This is the first step that
		// actually creates a resource.
		LoginResult loginResult;
		try {
			loginResult = logins.createSession(loginId, role);
		} catch (DomainException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			log.warn("SAGA: session creation failed for loginId {}", loginId);
			throw new ConflictException("session.notcreated", Map.of("object", "session"));
		}

		// Step 5: audit log. If this fails, the session created above
		// must be undone — logout(), not a generic Login compensate.
		try {
			auditlogs.create(cmd.actorRef(), cmd.actorType(), cmd.severity(), cmd.originSystem(), cmd.originService(),
				cmd.originComponent(), cmd.data(), cmd.description());
		} catch (DomainException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			log.warn("SAGA: auditlog create failed, logging out the created session. LoginId: {}", loginId);
			logins.logout(loginResult.sessionToken());
			log.warn("SAGA: session logged out for loginId {}", loginId);
			throw new ConflictException("auditlog.notcreated", Map.of("object", "auditlog"));
		}

		return loginResult;
	}

	private UserRole resolveRole(UUID personRef) {
		try {
			if (instructors.isInstructor(personRef)) {
				return UserRole.INSTRUCTOR;
			}
			if (apprentices.isApprentice(personRef)) {
				return UserRole.APPRENTICE;
			}
		} catch (DomainException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			log.warn("SAGA: role resolution failed for personRef {}", personRef);
			throw new ConflictException("login.role.resolution.failed", Map.of("personRef", personRef.toString()));
		}

		// Fail loudly — every Login is created by registration-saga
		// alongside an Instructor or Apprentice, so this means a data
		// -integrity problem, not a normal "no role" case.
		log.error("SAGA: personRef {} matched neither instructor nor apprentice — no fallback role", personRef);
		throw new NotFoundException("login.role.notfound", Map.of("personRef", personRef.toString()));
	}
}