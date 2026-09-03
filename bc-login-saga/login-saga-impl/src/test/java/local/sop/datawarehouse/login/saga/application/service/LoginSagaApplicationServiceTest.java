package local.sop.datawarehouse.login.saga.application.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import local.sop.datawarehouse.login.saga.application.api.dto.CreateAuditlogCmd;
import local.sop.datawarehouse.login.saga.application.api.dto.LoginCmd;
import local.sop.datawarehouse.login.saga.application.api.dto.LoginResult;
import local.sop.datawarehouse.login.saga.application.ports.out.apprentice.ApprenticePort;
import local.sop.datawarehouse.login.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.datawarehouse.login.saga.application.ports.out.consent.ConsentPort;
import local.sop.datawarehouse.login.saga.application.ports.out.instructor.InstructorPort;
import local.sop.datawarehouse.login.saga.application.ports.out.login.LoginPort;
import local.sop.datawarehouse.login.saga.application.ports.out.login.LoginPort.AuthenticationResult;
import local.sop.datawarehouse.sharedlib.enums.ActorType;
import local.sop.datawarehouse.sharedlib.enums.Severity;
import local.sop.datawarehouse.sharedlib.enums.UserRole;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.DomainException;
import local.sop.common.libs.sharedkernel.exceptions.ErrorCode;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

/**
 * CHANGED: fully rewritten. The old version was built entirely against
 * login(username,password) + compensate(id,class,sagaState), neither
 * of which exist anymore — see LoginSagaApplicationService's own
 * Javadoc for the authenticate()/createSession()/logout() reshape.
 * Also: @InjectMocks previously only had three @Mock fields
 * (loginPort, consentPort, auditlogPort) against a constructor that
 * now takes five — InstructorPort and ApprenticePort were both
 * missing, which would have left them null and thrown NPEs the moment
 * role resolution ran.
 */
@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "bc.qualifier=login-saga",
    "security.enabled=false"
})
class LoginSagaApplicationServiceTest {

    @Mock
    private LoginPort loginPort;

    @Mock
    private InstructorPort instructorPort;

    @Mock
    private ApprenticePort apprenticePort;

    @Mock
    private ConsentPort consentPort;

    @Mock
    private AuditlogPort auditlogPort;

    @InjectMocks
    private LoginSagaApplicationService loginSagaApplicationService;

    private LoginCmd validLoginCmd;

    private Validator validator;

    private static final UUID LOGIN_ID = UUID.randomUUID();
    private static final UUID PERSON_REF = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        validLoginCmd = new LoginCmd(
            "testuser",
            "testpass",
            UUID.randomUUID(),
            ActorType.USER,
            Severity.INFO,
            "test-system",
            "test-service",
            "test-component",
            "test-data",
            "test-description");

            validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private AuthenticationResult authResult() {
        return new AuthenticationResult(LOGIN_ID, PERSON_REF, "testuser");
    }

    // CHANGED: LoginResult.role is now a String — this helper keeps a
    // UserRole parameter for readable call sites (loginResult(UserRole.INSTRUCTOR)),
    // converting internally so the actual DTO shape stays correct.
    private LoginResult loginResult(UserRole role) {
        return new LoginResult(LOGIN_ID, PERSON_REF, "testuser", role.name(), "test-session-token",
            LocalDateTime.now(), LocalDateTime.now().plusHours(8));
    }

    /** Stubs the full happy path: authenticate -> instructor -> consent -> createSession -> audit. */
    private void stubHappyPathAsInstructor() {
        when(loginPort.authenticate("testuser", "testpass")).thenReturn(authResult());
        when(instructorPort.isInstructor(PERSON_REF)).thenReturn(true);
        when(consentPort.hasConsent(PERSON_REF)).thenReturn(true);
        when(loginPort.createSession(LOGIN_ID, UserRole.INSTRUCTOR)).thenReturn(loginResult(UserRole.INSTRUCTOR));
        lenient().when(auditlogPort.create(any(UUID.class), any(ActorType.class), any(Severity.class),
                anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(UUID.randomUUID());
    }

    // ── happy path ────────────────────────────────────────────────────────────

    @Nested
    class HappyPath {

        @Test
        void login_shouldReturnLoginResult_whenSuccessful() {
            stubHappyPathAsInstructor();

            LoginResult result = loginSagaApplicationService.login(validLoginCmd);

            assertNotNull(result);
            assertEquals(LOGIN_ID, result.loginId());
            assertEquals("INSTRUCTOR", result.role());
            verify(loginPort, times(1)).authenticate("testuser", "testpass");
            verify(consentPort, times(1)).hasConsent(PERSON_REF);
            verify(loginPort, times(1)).createSession(LOGIN_ID, UserRole.INSTRUCTOR);
            verify(auditlogPort, times(1)).create(any(UUID.class), any(ActorType.class), any(Severity.class),
                    anyString(), anyString(), anyString(), anyString(), anyString());
        }

        @Test
        void login_shouldPassCorrectFields_toAuditlog() {
            stubHappyPathAsInstructor();

            loginSagaApplicationService.login(validLoginCmd);

            verify(auditlogPort, times(1)).create(eq(validLoginCmd.actorRef()), any(ActorType.class), any(Severity.class),
                    eq("test-system"), eq("test-service"), eq("test-component"), eq("test-data"), eq("test-description"));
        }

        @Test
        void login_shouldResolveApprenticeRole_whenNotAnInstructor() {
            when(loginPort.authenticate("testuser", "testpass")).thenReturn(authResult());
            when(instructorPort.isInstructor(PERSON_REF)).thenReturn(false);
            when(apprenticePort.isApprentice(PERSON_REF)).thenReturn(true);
            when(consentPort.hasConsent(PERSON_REF)).thenReturn(true);
            when(loginPort.createSession(LOGIN_ID, UserRole.APPRENTICE)).thenReturn(loginResult(UserRole.APPRENTICE));
            when(auditlogPort.create(any(UUID.class), any(ActorType.class), any(Severity.class),
                    anyString(), anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(UUID.randomUUID());

            LoginResult result = loginSagaApplicationService.login(validLoginCmd);

            assertEquals("APPRENTICE", result.role());
            verify(loginPort).createSession(LOGIN_ID, UserRole.APPRENTICE);
        }

        @Test
        void login_shouldCheckInstructorBeforeApprentice() {
            stubHappyPathAsInstructor();

            loginSagaApplicationService.login(validLoginCmd);

            // Instructor found first — apprentice should never even be checked.
            verify(instructorPort).isInstructor(PERSON_REF);
            verify(apprenticePort, never()).isApprentice(any());
        }
    }

    // ── authenticate ──────────────────────────────────────────────────────────
    // Creates nothing — a failure here has nothing to compensate at all.

    @Nested
    class AuthenticateStep {

        @Test
        void shouldPropagateValidationException_unwrapped() {
            when(loginPort.authenticate("testuser", "testpass"))
                .thenThrow(new ValidationException("login.validation.failed", Map.of()));

            ValidationException exception = assertThrows(ValidationException.class,
                () -> loginSagaApplicationService.login(validLoginCmd));

            assertEquals("login.validation.failed", exception.getMessage());
            verifyNoInteractions(instructorPort, apprenticePort, consentPort, auditlogPort);
        }

        @Test
        void shouldPropagateDomainException_unwrapped() {
            when(loginPort.authenticate("testuser", "testpass"))
                .thenThrow(new DomainException(ErrorCode.CONFLICT, "login.domain.failed", Map.of()));

            DomainException exception = assertThrows(DomainException.class,
                () -> loginSagaApplicationService.login(validLoginCmd));

            assertEquals("login.domain.failed", exception.getMessage());
        }

        @Test
        void shouldWrapUnexpectedException_andCompensateNothing() {
            when(loginPort.authenticate("testuser", "testpass")).thenThrow(new RuntimeException("Login service down"));

            ConflictException exception = assertThrows(ConflictException.class,
                () -> loginSagaApplicationService.login(validLoginCmd));

            assertEquals("login.notcreated", exception.getMessage());
            // Nothing was created — nothing downstream should ever be called.
            verifyNoInteractions(instructorPort, apprenticePort, consentPort, auditlogPort);
            verify(loginPort, never()).createSession(any(), any());
            verify(loginPort, never()).logout(any());
        }
    }

    // ── role resolution ───────────────────────────────────────────────────────
    // Also creates nothing. The fail-loud rule (no TECHUSER/any
    // fallback for "found in neither") is the actual new business rule
    // this whole rework exists to enforce correctly.

    @Nested
    class RoleResolutionStep {

        @Test
        void shouldThrowNotFoundException_whenPersonMatchesNeitherRole() {
            when(loginPort.authenticate("testuser", "testpass")).thenReturn(authResult());
            when(instructorPort.isInstructor(PERSON_REF)).thenReturn(false);
            when(apprenticePort.isApprentice(PERSON_REF)).thenReturn(false);

            NotFoundException exception = assertThrows(NotFoundException.class,
                () -> loginSagaApplicationService.login(validLoginCmd));

            assertEquals("login.role.notfound", exception.getMessage());
            // No fallback role — consent/createSession/audit never run.
            verifyNoInteractions(consentPort, auditlogPort);
            verify(loginPort, never()).createSession(any(), any());
        }

        @Test
        void shouldWrapUnexpectedException_duringInstructorCheck() {
            when(loginPort.authenticate("testuser", "testpass")).thenReturn(authResult());
            when(instructorPort.isInstructor(PERSON_REF)).thenThrow(new RuntimeException("Instructor service down"));

            ConflictException exception = assertThrows(ConflictException.class,
                () -> loginSagaApplicationService.login(validLoginCmd));

            assertEquals("login.role.resolution.failed", exception.getMessage());
            verifyNoInteractions(consentPort, auditlogPort);
        }

        @Test
        void shouldWrapUnexpectedException_duringApprenticeCheck() {
            when(loginPort.authenticate("testuser", "testpass")).thenReturn(authResult());
            when(instructorPort.isInstructor(PERSON_REF)).thenReturn(false);
            when(apprenticePort.isApprentice(PERSON_REF)).thenThrow(new RuntimeException("Apprentice service down"));

            ConflictException exception = assertThrows(ConflictException.class,
                () -> loginSagaApplicationService.login(validLoginCmd));

            assertEquals("login.role.resolution.failed", exception.getMessage());
        }
    }

    // ── consent check ─────────────────────────────────────────────────────────
    // CHANGED: previously called loginPort.compensate(...) on failure —
    // now nothing has been created yet at this point (createSession
    // hasn't run), so there's nothing to compensate at all.

    @Nested
    class ConsentStep {

        @Test
        void shouldThrowConflictException_whenConsentNotGiven() {
            when(loginPort.authenticate("testuser", "testpass")).thenReturn(authResult());
            when(instructorPort.isInstructor(PERSON_REF)).thenReturn(true);
            when(consentPort.hasConsent(PERSON_REF)).thenReturn(false);

            ConflictException exception = assertThrows(ConflictException.class,
                () -> loginSagaApplicationService.login(validLoginCmd));

            assertEquals("consent.notgiven", exception.getMessage());
            verify(loginPort, never()).createSession(any(), any());
            verify(loginPort, never()).logout(any());
            verifyNoInteractions(auditlogPort);
        }

        @Test
        void shouldWrapUnexpectedException_whenConsentCheckThrows() {
            when(loginPort.authenticate("testuser", "testpass")).thenReturn(authResult());
            when(instructorPort.isInstructor(PERSON_REF)).thenReturn(true);
            when(consentPort.hasConsent(PERSON_REF)).thenThrow(new RuntimeException("Consent service down"));

            ConflictException exception = assertThrows(ConflictException.class,
                () -> loginSagaApplicationService.login(validLoginCmd));

            assertEquals("consent.check.failed", exception.getMessage());
            verify(loginPort, never()).createSession(any(), any());
            verifyNoInteractions(auditlogPort);
        }
    }

    // ── createSession ─────────────────────────────────────────────────────────

    @Nested
    class CreateSessionStep {

        @Test
        void shouldWrapUnexpectedException_andSkipAudit() {
            when(loginPort.authenticate("testuser", "testpass")).thenReturn(authResult());
            when(instructorPort.isInstructor(PERSON_REF)).thenReturn(true);
            when(consentPort.hasConsent(PERSON_REF)).thenReturn(true);
            when(loginPort.createSession(LOGIN_ID, UserRole.INSTRUCTOR))
                .thenThrow(new RuntimeException("Session creation failed"));

            ConflictException exception = assertThrows(ConflictException.class,
                () -> loginSagaApplicationService.login(validLoginCmd));

            assertEquals("session.notcreated", exception.getMessage());
            verifyNoInteractions(auditlogPort);
            verify(loginPort, never()).logout(any());
        }
    }

    // ── audit log ─────────────────────────────────────────────────────────────
    // CHANGED: previously called loginPort.compensate(id, class,
    // sagaState) — now calls logout(sessionToken) on the session that
    // was just created, since that's the actual thing to undo (a
    // generic Login-level compensate no longer exists on LoginPort at
    // all).

    @Nested
    class AuditlogStep {

        @Test
        void shouldLogoutTheCreatedSession_whenAuditFails() {
            stubHappyPathAsInstructor();
            when(auditlogPort.create(any(UUID.class), any(ActorType.class), any(Severity.class),
                    anyString(), anyString(), anyString(), anyString(), anyString()))
                    .thenThrow(new RuntimeException("Auditlog service down"));

            ConflictException exception = assertThrows(ConflictException.class,
                () -> loginSagaApplicationService.login(validLoginCmd));

            assertEquals("auditlog.notcreated", exception.getMessage());
            verify(loginPort, times(1)).logout("test-session-token");
        }
    }

    // ── unrelated bean-validation check ──────────────────────────────────────

    @Test
    void shouldPassValidation_whenCreateAuditlogCmdIsValid() {
            CreateAuditlogCmd cmd = new CreateAuditlogCmd(
                            UUID.randomUUID(),
                            ActorType.USER,
                            Severity.INFO,
                            "system",
                            "service",
                            "component",
                            "data",
                            "description");

            var violations = validator.validate(cmd);

            assertTrue(violations.isEmpty());
    }

}