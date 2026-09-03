package local.sop.datawarehouse.login.application.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import local.sop.datawarehouse.login.application.api.LoginDirectory;
import local.sop.datawarehouse.login.application.api.dto.CreateLoginCmd;
import local.sop.datawarehouse.login.application.api.dto.CreatedLoginResult;
import local.sop.datawarehouse.login.application.api.dto.LoginCmd;
import local.sop.datawarehouse.login.application.api.dto.LoginResult;
import local.sop.datawarehouse.login.application.api.dto.LogoutCmd;
import local.sop.datawarehouse.login.application.api.dto.SessionValidationResult;
import local.sop.datawarehouse.login.application.api.dto.ValidateSessionCmd;
import local.sop.datawarehouse.login.application.service.util.PasswordGenerator;
import local.sop.datawarehouse.login.domain.model.Login;
import local.sop.datawarehouse.login.domain.model.Session;
import local.sop.datawarehouse.login.domain.model.valueobjects.ExpiresAtTimestamp;
import local.sop.datawarehouse.login.domain.model.valueobjects.LoginId;
import local.sop.datawarehouse.login.domain.model.valueobjects.PlainPassword;
import local.sop.datawarehouse.login.domain.model.valueobjects.SessionId;
import local.sop.datawarehouse.login.domain.model.valueobjects.SessionToken;
import local.sop.datawarehouse.login.domain.model.valueobjects.Username;
import local.sop.datawarehouse.login.domain.ports.out.LoginRepositoryPort;
import local.sop.datawarehouse.login.domain.ports.out.SessionRepositoryPort;
import local.sop.datawarehouse.login.domain.service.LoginDomain;
import local.sop.datawarehouse.sharedlib.enums.LoginStatus;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;

@Service
public class LoginApplicationService implements LoginDirectory {

    private static final Logger log =
        LoggerFactory.getLogger(LoginApplicationService.class);

    private final LoginRepositoryPort repo;
    private final SessionRepositoryPort sessionRepo;
    private final LoginDomain domain;
    private final PasswordEncoder encoder;
    private final PasswordGenerator passwordGenerator;

    LoginApplicationService(
            LoginRepositoryPort repo,
            SessionRepositoryPort sessionRepo,
            LoginDomain domain,
            PasswordEncoder encoder,
            PasswordGenerator passwordGenerator) {
        this.repo = repo;
        this.sessionRepo = sessionRepo;
        this.domain = domain;
        this.encoder = encoder;
        this.passwordGenerator = passwordGenerator;
    }

    // ── createLogin ────────────────────────────────────────────────────────────

    @Transactional
    @Override
    public CreatedLoginResult createLogin(CreateLoginCmd cmd) {
        try {
            String pwd = passwordGenerator.generate();
            PlainPassword.of(pwd); // validate policy before encoding

            Login login = domain.createLogin(
                cmd.personRef(),
                cmd.username(),
                encoder.encode(pwd),
                LoginStatus.parse(cmd.status()));

            login = repo.save(login);
            log.info("Login created {}", login);
            return new CreatedLoginResult(
                login.getId().value(), pwd, login.getCreatedAt().value());

        } catch (ValidationException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.warn("Error in createLogin", ex);
            throw new ValidationException("login.create.failed",
                Map.of("function", "createLogin"));
        }
    }

    // ── login ──────────────────────────────────────────────────────────────────

    @Transactional
    @Override
    public LoginResult login(LoginCmd query) {
        try {
            Login login = repo.findByUsername(Username.of(query.username()));

            if (login == null) {
                throw new ValidationException("login.credentials.invalid",
                    Map.of("username", query.username()));
            }

            if (login.getStatus() != LoginStatus.ACTIVATED) {
                throw new ValidationException("login.deactivated",
                    Map.of("username", query.username()));
            }

            if (!encoder.matches(query.password(), login.getPassword().value())) {
                throw new ValidationException("login.credentials.invalid",
                    Map.of("username", query.username()));
            }

            /*
             * SSO check — if a valid unexpired session already exists for this
             * login, return it. The same token is returned to the calling handler
             * regardless of which device initiated the login.
             */
            Optional<Session> existingSession =
                sessionRepo.findActiveByLoginId(login.getId());

            if (existingSession.isPresent()) {
                Session existing = existingSession.get();
                log.info("SSO: returning existing valid session for username: {}, " +
                    "token prefix: {}", query.username(),
                    existing.getToken().value().substring(0, 8));
                return new LoginResult(
                    login.getId().value(),
                    login.getPersonRef().value(),
                    login.getUsername().value(),
                    existing.getToken().value(),
                    existing.getCreatedAt().value(),
                    existing.getExpiresAt().value());
            }

            /*
             * No valid session — clean up expired sessions and create a new one.
             */
            sessionRepo.deleteByLoginId(login.getId());

            String sessionToken = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(8);

            Session session = Session.builder()
                .id(new SessionId(UUID.randomUUID()))
                .login(login)
                .sessionToken(new SessionToken(sessionToken))
                .expiresAt(new ExpiresAtTimestamp(expiresAt))
                .build();

            sessionRepo.save(session);
            log.info("Login successful for username: {}, session created with " +
                "token prefix: {}", query.username(), sessionToken.substring(0, 8));

            return new LoginResult(
                login.getId().value(),
                login.getPersonRef().value(),
                login.getUsername().value(),
                session.getToken().value(),
                session.getCreatedAt().value(),
                session.getExpiresAt().value());

        } catch (ValidationException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.warn("Error in login", ex);
            throw new ValidationException("login.failed",
                Map.of("function", "login"));
        }
    }

    // ── logout ─────────────────────────────────────────────────────────────────

    @Transactional
    @Override
    public void logout(LogoutCmd query) {
        try {
            SessionToken token = new SessionToken(query.sessionToken());
            boolean deleted = sessionRepo.deleteBySessionToken(token);

            if (!deleted) {
                throw new NotFoundException("session.not.found",
                    Map.of("key", "token"));
            }

            log.info("Logout successful for session token prefix: {}",
                query.sessionToken().substring(0, 8));

        } catch (NotFoundException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.warn("Error in logout", ex);
            throw new ValidationException("logout.failed",
                Map.of("function", "logout"));
        }
    }

    // ── validateSession ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    @Override
    public SessionValidationResult validateSession(ValidateSessionCmd cmd) {
        try {
            SessionToken token = new SessionToken(cmd.sessionToken());

            Optional<Session> found = sessionRepo.findBySessionToken(token);

            if (found.isEmpty()) {
                log.info("Session not found for token prefix: {}",
                    cmd.sessionToken().substring(0, 8));
                return SessionValidationResult.invalid();
            }

            Session session = found.get();

            if (session.getExpiresAt().value().isBefore(LocalDateTime.now())) {
                /*
                 * Session expired — clean up and return invalid.
                 * @Transactional(readOnly=true) prevents writes, so we promote
                 * cleanup to the caller or accept the stale row until next login.
                 * The session will be cleaned up on the next login() call via
                 * deleteByLoginId() before a new session is created.
                 */
                log.info("Session expired for token prefix: {}",
                    cmd.sessionToken().substring(0, 8));
                return SessionValidationResult.invalid();
            }

            Login login = session.getLogin();
            log.info("Session valid for username: {}, expires: {}",
                login.getUsername().value(),
                session.getExpiresAt().value());

            return new SessionValidationResult(
                true,
                login.getId().value(),
                login.getPersonRef().value(),
                login.getUsername().value(),
                session.getExpiresAt().value());

        } catch (ValidationException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.warn("Error in validateSession", ex);
            throw new ValidationException("session.validate.failed",
                Map.of("function", "validateSession"));
        }
    }

    // ── compensate ─────────────────────────────────────────────────────────────

    @Transactional
    @Override
    public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        log.info("Compensate called from class {}", clazz.getSimpleName());
        var login = repo.findById(LoginId.of(id));
        if (login.isEmpty()) {
            return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
        }
        boolean result = repo.compensate(LoginId.of(id), sagaState);
        if (result) {
            return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
        }
        return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
    }
}