package local.sop.datawarehouse.login.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import local.sop.datawarehouse.login.application.api.dto.*;
import local.sop.datawarehouse.login.application.service.util.PasswordGenerator;
import local.sop.datawarehouse.login.domain.model.Login;
import local.sop.datawarehouse.login.domain.model.Session;
import local.sop.datawarehouse.login.domain.model.valueobjects.*;
import local.sop.datawarehouse.login.domain.ports.out.LoginRepositoryPort;
import local.sop.datawarehouse.login.domain.ports.out.SessionRepositoryPort;
import local.sop.datawarehouse.login.domain.service.LoginDomain;
import local.sop.datawarehouse.sharedlib.enums.LoginStatus;
import local.sop.datawarehouse.sharedlib.enums.UserRole;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;

@ExtendWith(MockitoExtension.class)
class LoginApplicationServiceTest {

    @Mock private LoginRepositoryPort repo;
    @Mock private SessionRepositoryPort sessionRepo;
    @Mock private LoginDomain domain;
    @Mock private PasswordGenerator passwordGenerator;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private LoginApplicationService service;

    private static final String USERNAME      = "nick579a@zbc.dk";
    private static final String PLAIN_PWD     = "Password5!";
    private static final UUID   PERSON_REF    = UUID.randomUUID();
    private static final UUID   LOGIN_ID_VAL  = UUID.randomUUID();
    private static final String SESSION_TOKEN_VAL = UUID.randomUUID().toString();

    private Login activeLogin;
    private Session validSession;

    @BeforeEach
    void setUp() {
        service = new LoginApplicationService(
            repo, sessionRepo, domain, encoder, passwordGenerator);

        String hash = encoder.encode(PLAIN_PWD);

        activeLogin = Login.builder()
            .id(new LoginId(LOGIN_ID_VAL))
            .personRef(new PersonRef(PERSON_REF))
            .username(new Username(USERNAME))
            .password(HashedPassword.of(hash))
            .status(LoginStatus.ACTIVATED)
            .build();

        // CHANGED: role added. Without it, this @BeforeEach itself
        // throws ValidationException (the domain Session.Builder
        // requires role) — every single test in this class was
        // failing at setup before this fix, not just the ones that
        // exercise role-specific behavior.
        validSession = Session.builder()
            .id(new SessionId(UUID.randomUUID()))
            .login(activeLogin)
            .sessionToken(new SessionToken(SESSION_TOKEN_VAL))
            .expiresAt(new ExpiresAtTimestamp(LocalDateTime.now().plusHours(8)))
            .role(UserRole.INSTRUCTOR)
            .build();
    }

    // ── createLogin ────────────────────────────────────────────────────────────

    @Nested
    class CreateLogin {

        @Test
        void shouldCreateLogin_andReturnGeneratedPassword() {
            when(passwordGenerator.generate()).thenReturn(PLAIN_PWD);
            when(domain.createLogin(any(), any(), any(), any())).thenReturn(activeLogin);
            when(repo.save(any())).thenReturn(activeLogin);

            var result = service.createLogin(
                new CreateLoginCmd(PERSON_REF, USERNAME, "ACTIVATED"));

            assertNotNull(result);
            assertEquals(LOGIN_ID_VAL, result.id());
            assertEquals(PLAIN_PWD, result.password());
            verify(repo).save(any());
        }

        @Test
        void shouldEncodePassword_beforeSaving() {
            when(passwordGenerator.generate()).thenReturn(PLAIN_PWD);
            when(domain.createLogin(any(), any(), any(), any())).thenReturn(activeLogin);
            when(repo.save(any())).thenReturn(activeLogin);

            service.createLogin(new CreateLoginCmd(PERSON_REF, USERNAME, "ACTIVATED"));

            verify(domain).createLogin(
                eq(PERSON_REF), eq(USERNAME),
                argThat(pwd -> encoder.matches(PLAIN_PWD, pwd)),
                eq(LoginStatus.ACTIVATED));
        }

        @Test
        void shouldThrowValidationException_whenSaveFails() {
            when(passwordGenerator.generate()).thenReturn(PLAIN_PWD);
            when(domain.createLogin(any(), any(), any(), any())).thenReturn(activeLogin);
            when(repo.save(any())).thenThrow(new RuntimeException("db crash"));

            assertThrows(ValidationException.class, () ->
                service.createLogin(new CreateLoginCmd(PERSON_REF, USERNAME, "ACTIVATED")));
        }

        @Test
        void shouldNeverSave_whenDomainThrows() {
            when(passwordGenerator.generate()).thenReturn(PLAIN_PWD);
            when(domain.createLogin(any(), any(), any(), any()))
                .thenThrow(new ValidationException("login.username.invalid",
                    Map.of("field", "username")));

            assertThrows(ValidationException.class, () ->
                service.createLogin(new CreateLoginCmd(PERSON_REF, USERNAME, "ACTIVATED")));

            verify(repo, never()).save(any());
        }
    }

    // ── authenticate ───────────────────────────────────────────────────────────
    // CHANGED: was the "login" @Nested class calling service.login(new
    // LoginCmd(...)) — that method/DTO no longer exist. This half of
    // the old flow covers credential verification only, so it never
    // touches sessionRepo at all (see shouldNotTouchSessionRepo below).

    @Nested
    class Authenticate {

        @Test
        void shouldReturnAuthenticationResult_whenCredentialsValid() {
            when(repo.findByUsername(any())).thenReturn(activeLogin);

            AuthenticationResult result =
                service.authenticate(new AuthenticateCmd(USERNAME, PLAIN_PWD));

            assertNotNull(result);
            assertEquals(LOGIN_ID_VAL, result.loginId());
            assertEquals(PERSON_REF, result.personRef());
            assertEquals(USERNAME, result.username());
        }

        @Test
        void shouldNotTouchSessionRepo() {
            when(repo.findByUsername(any())).thenReturn(activeLogin);

            service.authenticate(new AuthenticateCmd(USERNAME, PLAIN_PWD));

            verifyNoInteractions(sessionRepo);
        }

        @Test
        void shouldThrowValidationException_whenUsernameNotFound() {
            when(repo.findByUsername(any())).thenReturn(null);

            assertThrows(ValidationException.class, () ->
                service.authenticate(new AuthenticateCmd(USERNAME, PLAIN_PWD)));
        }

        @Test
        void shouldThrowValidationException_whenLoginIsDeactivated() {
            Login deactivated = activeLogin.withStatus(LoginStatus.DEACTIVATED);
            when(repo.findByUsername(any())).thenReturn(deactivated);

            ValidationException ex = assertThrows(ValidationException.class, () ->
                service.authenticate(new AuthenticateCmd(USERNAME, PLAIN_PWD)));

            assertEquals("login.deactivated", ex.messageKey());
        }

        @Test
        void shouldThrowValidationException_whenPasswordIsWrong() {
            when(repo.findByUsername(any())).thenReturn(activeLogin);

            assertThrows(ValidationException.class, () ->
                service.authenticate(new AuthenticateCmd(USERNAME, "WrongPassword5!")));
        }

        @Test
        void shouldUseGenericError_forBothUsernameAndPasswordFailures() {
            when(repo.findByUsername(any())).thenReturn(null);
            ValidationException notFound = assertThrows(ValidationException.class, () ->
                service.authenticate(new AuthenticateCmd(USERNAME, PLAIN_PWD)));

            when(repo.findByUsername(any())).thenReturn(activeLogin);
            ValidationException wrongPwd = assertThrows(ValidationException.class, () ->
                service.authenticate(new AuthenticateCmd(USERNAME, "WrongPassword5!")));

            assertEquals(notFound.messageKey(), wrongPwd.messageKey(),
                "Same error key prevents username enumeration");
        }
    }

    // ── createSession ──────────────────────────────────────────────────────────
    // CHANGED: the other half of the old login() flow — takes an
    // already-authenticated loginId plus a role resolved by the
    // caller. Keeps all the original SSO reuse-existing-session
    // coverage, translated to the new signature, plus new coverage for
    // role itself.

    @Nested
    class CreateSessionTests {

        @Test
        void shouldReturnLoginResult_whenNoExistingSession() {
            when(repo.findById(LoginId.of(LOGIN_ID_VAL))).thenReturn(Optional.of(activeLogin));
            when(sessionRepo.findActiveByLoginId(any())).thenReturn(Optional.empty());

            LoginResult result =
                service.createSession(new CreateSessionCmd(LOGIN_ID_VAL, "INSTRUCTOR"));

            assertNotNull(result);
            assertEquals(USERNAME, result.username());
            assertEquals("INSTRUCTOR", result.role());
            assertNotNull(result.sessionToken());
            verify(sessionRepo).save(any());
        }

        @Test
        void shouldThrowNotFoundException_whenLoginDoesNotExist() {
            when(repo.findById(LoginId.of(LOGIN_ID_VAL))).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                service.createSession(new CreateSessionCmd(LOGIN_ID_VAL, "INSTRUCTOR")));
        }

        @Test
        void shouldReturnExistingSession_whenValidSessionAlreadyExists() {
            when(repo.findById(LoginId.of(LOGIN_ID_VAL))).thenReturn(Optional.of(activeLogin));
            when(sessionRepo.findActiveByLoginId(any())).thenReturn(Optional.of(validSession));

            LoginResult result =
                service.createSession(new CreateSessionCmd(LOGIN_ID_VAL, "INSTRUCTOR"));

            assertEquals(SESSION_TOKEN_VAL, result.sessionToken());
            assertEquals(validSession.getRole().name(), result.role());
            verify(sessionRepo, never()).save(any());
            verify(sessionRepo, never()).deleteByLoginId(any());
        }

        @Test
        void shouldReturnSameToken_onSecondCallFromDifferentDevice() {
            when(repo.findById(LoginId.of(LOGIN_ID_VAL))).thenReturn(Optional.of(activeLogin));
            when(sessionRepo.findActiveByLoginId(any())).thenReturn(Optional.of(validSession));

            LoginResult first  = service.createSession(new CreateSessionCmd(LOGIN_ID_VAL, "INSTRUCTOR"));
            LoginResult second = service.createSession(new CreateSessionCmd(LOGIN_ID_VAL, "INSTRUCTOR"));

            assertEquals(first.sessionToken(), second.sessionToken());
            verify(sessionRepo, never()).save(any());
        }

        @Test
        void shouldCleanupExpiredSessions_beforeCreatingNew() {
            when(repo.findById(LoginId.of(LOGIN_ID_VAL))).thenReturn(Optional.of(activeLogin));
            when(sessionRepo.findActiveByLoginId(any())).thenReturn(Optional.empty());

            service.createSession(new CreateSessionCmd(LOGIN_ID_VAL, "INSTRUCTOR"));

            var inOrder = inOrder(sessionRepo);
            inOrder.verify(sessionRepo).deleteByLoginId(new LoginId(LOGIN_ID_VAL));
            inOrder.verify(sessionRepo).save(any());
        }

        // NEW: role is stored on the newly created session, not just
        // echoed back in the response — this is the actual point of
        // the SSO rework (every portal's later validateSession() call
        // reads role from the persisted Session, not from this
        // response).
        @Test
        void shouldStoreGivenRole_onNewSession() {
            when(repo.findById(LoginId.of(LOGIN_ID_VAL))).thenReturn(Optional.of(activeLogin));
            when(sessionRepo.findActiveByLoginId(any())).thenReturn(Optional.empty());

            service.createSession(new CreateSessionCmd(LOGIN_ID_VAL, "APPRENTICE"));

            ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
            verify(sessionRepo).save(captor.capture());
            assertEquals(UserRole.APPRENTICE, captor.getValue().getRole());
        }
    }

    // ── logout ─────────────────────────────────────────────────────────────────

    @Nested
    class Logout {

        @Test
        void shouldLogoutSuccessfully_whenSessionExists() {
            when(sessionRepo.deleteBySessionToken(any())).thenReturn(true);

            assertDoesNotThrow(() ->
                service.logout(new LogoutCmd(SESSION_TOKEN_VAL)));

            verify(sessionRepo).deleteBySessionToken(new SessionToken(SESSION_TOKEN_VAL));
        }

        @Test
        void shouldThrowNotFoundException_whenSessionDoesNotExist() {
            when(sessionRepo.deleteBySessionToken(any())).thenReturn(false);

            assertThrows(NotFoundException.class, () ->
                service.logout(new LogoutCmd(SESSION_TOKEN_VAL)));
        }

        @Test
        void shouldCallDeleteExactlyOnce() {
            when(sessionRepo.deleteBySessionToken(any())).thenReturn(true);

            service.logout(new LogoutCmd(SESSION_TOKEN_VAL));

            verify(sessionRepo, times(1)).deleteBySessionToken(any());
            verify(sessionRepo, never()).findBySessionToken(any());
        }

        @Test
        void shouldThrowValidationException_whenRepoThrows() {
            when(sessionRepo.deleteBySessionToken(any()))
                .thenThrow(new RuntimeException("db crash"));

            assertThrows(ValidationException.class, () ->
                service.logout(new LogoutCmd(SESSION_TOKEN_VAL)));
        }
    }

    // ── validateSession ────────────────────────────────────────────────────────

    @Nested
    class ValidateSession {

        @Test
        void shouldReturnValid_whenSessionExistsAndNotExpired() {
            when(sessionRepo.findBySessionToken(any())).thenReturn(Optional.of(validSession));

            SessionValidationResult result =
                service.validateSession(new ValidateSessionCmd(SESSION_TOKEN_VAL));

            assertTrue(result.valid());
            assertEquals(LOGIN_ID_VAL, result.loginId());
            assertEquals(PERSON_REF, result.personRef());
            assertEquals(USERNAME, result.username());
            // CHANGED: role assertion added — the actual point of the
            // SSO rework is that this comes back for every portal to
            // make an authorization decision with.
            assertEquals("INSTRUCTOR", result.role());
            assertNotNull(result.expiresAt());
        }

        @Test
        void shouldReturnInvalid_whenSessionNotFound() {
            when(sessionRepo.findBySessionToken(any())).thenReturn(Optional.empty());

            SessionValidationResult result =
                service.validateSession(new ValidateSessionCmd(SESSION_TOKEN_VAL));

            assertFalse(result.valid());
            assertNull(result.loginId());
            assertNull(result.personRef());
            assertNull(result.username());
        }

        @Test
        void shouldReturnInvalid_whenSessionIsExpired() {
            // Mocked Session — getRole() is never reached here, since
            // the expiry check short-circuits before validateSession()
            // ever reads role, so it doesn't need stubbing.
            Session expiredSession = mock(Session.class);
            ExpiresAtTimestamp expiredAt = mock(ExpiresAtTimestamp.class);

            when(expiredSession.getExpiresAt()).thenReturn(expiredAt);
            when(expiredAt.value()).thenReturn(LocalDateTime.now().minusHours(1));
            when(sessionRepo.findBySessionToken(any())).thenReturn(Optional.of(expiredSession));

            SessionValidationResult result =
                service.validateSession(new ValidateSessionCmd(SESSION_TOKEN_VAL));

            assertFalse(result.valid());
        }

        @Test
        void shouldReturnLoginDetails_fromSessionLogin() {
            when(sessionRepo.findBySessionToken(any())).thenReturn(Optional.of(validSession));

            SessionValidationResult result =
                service.validateSession(new ValidateSessionCmd(SESSION_TOKEN_VAL));

            assertEquals(activeLogin.getId().value(), result.loginId());
            assertEquals(activeLogin.getPersonRef().value(), result.personRef());
            assertEquals(activeLogin.getUsername().value(), result.username());
        }

        @Test
        void shouldNeverModifyState_onValidation() {
            when(sessionRepo.findBySessionToken(any())).thenReturn(Optional.of(validSession));

            service.validateSession(new ValidateSessionCmd(SESSION_TOKEN_VAL));

            verify(sessionRepo, never()).save(any());
            verify(sessionRepo, never()).deleteBySessionToken(any());
            verify(sessionRepo, never()).deleteByLoginId(any());
        }

        @Test
        void shouldThrowValidationException_whenRepoThrows() {
            when(sessionRepo.findBySessionToken(any()))
                .thenThrow(new RuntimeException("db crash"));

            assertThrows(ValidationException.class, () ->
                service.validateSession(new ValidateSessionCmd(SESSION_TOKEN_VAL)));
        }
    }

    // ── updateLoginStatus ──────────────────────────────────────────────────────
    // NEW: coverage for the general-purpose disable/re-activate
    // capability added as part of the tech-user lockout work.

    @Nested
    class UpdateLoginStatus {

        @Test
        void shouldDeactivateLogin_andSave() {
            when(repo.findById(LoginId.of(LOGIN_ID_VAL))).thenReturn(Optional.of(activeLogin));

            service.updateLoginStatus(new UpdateLoginStatusCmd(LOGIN_ID_VAL, "DEACTIVATED"));

            ArgumentCaptor<Login> captor = ArgumentCaptor.forClass(Login.class);
            verify(repo).save(captor.capture());
            assertEquals(LoginStatus.DEACTIVATED, captor.getValue().getStatus());
        }

        @Test
        void shouldClearActiveSession_whenDeactivating() {
            when(repo.findById(LoginId.of(LOGIN_ID_VAL))).thenReturn(Optional.of(activeLogin));

            service.updateLoginStatus(new UpdateLoginStatusCmd(LOGIN_ID_VAL, "DEACTIVATED"));

            verify(sessionRepo).deleteByLoginId(activeLogin.getId());
        }

        @Test
        void shouldNotClearSession_whenReactivating() {
            Login deactivated = activeLogin.withStatus(LoginStatus.DEACTIVATED);
            when(repo.findById(LoginId.of(LOGIN_ID_VAL))).thenReturn(Optional.of(deactivated));

            service.updateLoginStatus(new UpdateLoginStatusCmd(LOGIN_ID_VAL, "ACTIVATED"));

            verify(sessionRepo, never()).deleteByLoginId(any());
        }

        @Test
        void shouldThrowNotFoundException_whenLoginDoesNotExist() {
            when(repo.findById(LoginId.of(LOGIN_ID_VAL))).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                service.updateLoginStatus(new UpdateLoginStatusCmd(LOGIN_ID_VAL, "DEACTIVATED")));
        }

        @Test
        void shouldThrowValidationException_whenRepoThrows() {
            when(repo.findById(LoginId.of(LOGIN_ID_VAL))).thenReturn(Optional.of(activeLogin));
            when(repo.save(any())).thenThrow(new RuntimeException("db crash"));

            assertThrows(ValidationException.class, () ->
                service.updateLoginStatus(new UpdateLoginStatusCmd(LOGIN_ID_VAL, "DEACTIVATED")));
        }
    }

    // ── compensate ─────────────────────────────────────────────────────────────

    @Nested
    class Compensate {

        @Test
        void shouldReturnIdempotentFalse_whenLoginNotFound() {
            UUID id = UUID.randomUUID();
            when(repo.findById(LoginId.of(id))).thenReturn(Optional.empty());

            ResponseCompensated result =
                service.compensate(id, Login.class, SagaOutcome.COMPENSATED);

            assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
            assertFalse(result.success());
        }

        @Test
        void shouldReturnCompensated_whenDeleteSucceeds() {
            UUID id = UUID.randomUUID();
            when(repo.findById(LoginId.of(id))).thenReturn(Optional.of(activeLogin));
            when(repo.compensate(LoginId.of(id), SagaOutcome.COMPENSATED)).thenReturn(true);

            ResponseCompensated result =
                service.compensate(id, Login.class, SagaOutcome.COMPENSATED);

            assertEquals(SagaOutcome.COMPENSATED, result.sagaState());
            assertTrue(result.success());
        }

        @Test
        void shouldReturnIdempotentTrue_whenDeleteReturnsZero() {
            UUID id = UUID.randomUUID();
            when(repo.findById(LoginId.of(id))).thenReturn(Optional.of(activeLogin));
            when(repo.compensate(LoginId.of(id), SagaOutcome.COMPENSATED)).thenReturn(false);

            ResponseCompensated result =
                service.compensate(id, Login.class, SagaOutcome.COMPENSATED);

            assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
            assertTrue(result.success());
        }
    }
}