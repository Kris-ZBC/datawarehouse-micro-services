package local.sop.datawarehouse.login.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import local.sop.datawarehouse.login.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.datawarehouse.login.domain.model.valueobjects.ExpiresAtTimestamp;
import local.sop.datawarehouse.login.domain.model.valueobjects.HashedPassword;
import local.sop.datawarehouse.login.domain.model.valueobjects.PersonRef;
import local.sop.datawarehouse.login.domain.model.valueobjects.SessionToken;
import local.sop.datawarehouse.login.domain.model.valueobjects.Username;
import local.sop.datawarehouse.sharedlib.enums.LoginStatus;
import local.sop.common.libs.sharedkernel.enums.UserRole;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

class SessionTest {

    private static final String VALID_HASH = "$2b$10$" + "a".repeat(53);

    private Login activeLogin;
    private Login anotherLogin;

    @BeforeEach
    void setUp() {
        activeLogin = Login.builder()
            .personRef(new PersonRef(UUID.randomUUID()))
            .username(new Username("nick579a@zbc.dk"))
            .password(HashedPassword.of(VALID_HASH))
            .status(LoginStatus.ACTIVATED)
            .build();

        anotherLogin = Login.builder()
            .personRef(new PersonRef(UUID.randomUUID()))
            .username(new Username("john.doe@zbc.dk"))
            .password(HashedPassword.of(VALID_HASH))
            .status(LoginStatus.ACTIVATED)
            .build();
    }

    // ── happy path ─────────────────────────────────────────────────────────────

    @Test
    void shouldCreateSession_whenAllFieldsAreValid() {
        String token    = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(8);

        Session session = Session.builder()
            .login(activeLogin)
            .sessionToken(new SessionToken(token))
            .expiresAt(new ExpiresAtTimestamp(expiresAt))
            .role(UserRole.INSTRUCTOR)
            .build();

        assertNotNull(session.getId());
        assertEquals(activeLogin.getId(), session.getLogin().getId());
        assertEquals(activeLogin, session.getLogin());
        assertEquals(token, session.getToken().value());
        assertEquals(expiresAt, session.getExpiresAt().value());
        assertEquals(UserRole.INSTRUCTOR, session.getRole());
    }

    @Test
    void shouldGenerateId_whenIdIsNotProvided() {
        Session session = Session.builder()
            .login(activeLogin)
            .sessionToken(new SessionToken(UUID.randomUUID().toString()))
            .expiresAt(new ExpiresAtTimestamp(LocalDateTime.now().plusHours(8)))
            .role(UserRole.INSTRUCTOR)
            .build();

        assertNotNull(session.getId());
    }

    @Test
    void shouldGenerateCreatedAt_whenCreatedAtIsNull() {
        Session session = Session.builder()
            .login(activeLogin)
            .sessionToken(new SessionToken(UUID.randomUUID().toString()))
            .createdAt(null)
            .expiresAt(new ExpiresAtTimestamp(LocalDateTime.now().plusHours(8)))
            .role(UserRole.INSTRUCTOR)
            .build();

        assertNotNull(session.getCreatedAt());
    }

    // ── convenience accessor ───────────────────────────────────────────────────

    @Test
    void shouldReturnLoginId_viaConvenienceAccessor() {
        Session session = Session.builder()
            .login(activeLogin)
            .sessionToken(new SessionToken(UUID.randomUUID().toString()))
            .expiresAt(new ExpiresAtTimestamp(LocalDateTime.now().plusHours(8)))
            .role(UserRole.INSTRUCTOR)
            .build();

        assertEquals(activeLogin.getId(), session.getLogin().getId());
    }

    // ── with-methods ───────────────────────────────────────────────────────────

    @Test
    void shouldReturnNewInstance_whenLoginIsChanged() {
        Session original = buildSession(activeLogin, LocalDateTime.now().plusHours(8));
        Session updated  = original.withLogin(anotherLogin);

        assertNotSame(original, updated);
        assertEquals(anotherLogin, updated.getLogin());
        assertEquals(activeLogin, original.getLogin());
        assertEquals(original.getId(), updated.getId());
    }

    @Test
    void shouldReturnNewInstance_whenSessionTokenIsChanged() {
        SessionToken originalToken = new SessionToken(UUID.randomUUID().toString());
        SessionToken newToken      = new SessionToken(UUID.randomUUID().toString());

        Session original = Session.builder()
            .login(activeLogin)
            .sessionToken(originalToken)
            .expiresAt(new ExpiresAtTimestamp(LocalDateTime.now().plusHours(8)))
            .role(UserRole.INSTRUCTOR)
            .build();

        Session updated = original.withSessionToken(newToken);

        assertNotSame(original, updated);
        assertEquals(newToken, updated.getToken());
        assertEquals(originalToken, original.getToken());
        assertEquals(original.getId(), updated.getId());
    }

    @Test
    void shouldReturnNewInstance_whenCreatedAtIsChanged() {
        CreatedAtTimestamp originalCreatedAt = new CreatedAtTimestamp(
            LocalDateTime.now().minusHours(1));
        CreatedAtTimestamp newCreatedAt = new CreatedAtTimestamp(LocalDateTime.now());

        Session original = Session.builder()
            .login(activeLogin)
            .sessionToken(new SessionToken(UUID.randomUUID().toString()))
            .createdAt(originalCreatedAt)
            .expiresAt(new ExpiresAtTimestamp(LocalDateTime.now().plusHours(8)))
            .role(UserRole.INSTRUCTOR)
            .build();

        Session updated = original.withCreatedAt(newCreatedAt);

        assertNotSame(original, updated);
        assertEquals(newCreatedAt, updated.getCreatedAt());
        assertEquals(originalCreatedAt, original.getCreatedAt());
    }

    @Test
    void shouldReturnNewInstance_whenExpiresAtIsChanged() {
        ExpiresAtTimestamp originalExpiresAt = new ExpiresAtTimestamp(
            LocalDateTime.now().plusHours(8));
        ExpiresAtTimestamp newExpiresAt = new ExpiresAtTimestamp(
            LocalDateTime.now().plusHours(16));

        Session original = buildSession(activeLogin, originalExpiresAt.value());
        Session updated  = original.withExpiresAt(newExpiresAt);

        assertNotSame(original, updated);
        assertEquals(newExpiresAt, updated.getExpiresAt());
        assertEquals(originalExpiresAt, original.getExpiresAt());
        assertEquals(original.getId(), updated.getId());
    }

    // NEW: mirrors the other with-method tests, for the role field
    // added alongside the SSO rework.
    @Test
    void shouldReturnNewInstance_whenRoleIsChanged() {
        Session original = buildSession(activeLogin, LocalDateTime.now().plusHours(8));
        Session updated  = original.withRole(UserRole.APPRENTICE);

        assertNotSame(original, updated);
        assertEquals(UserRole.APPRENTICE, updated.getRole());
        assertEquals(UserRole.INSTRUCTOR, original.getRole());
        assertEquals(original.getId(), updated.getId());
    }

    // ── builder validation ─────────────────────────────────────────────────────

    @Test
    void shouldThrowException_whenLoginIsNull() {
        assertThrows(ValidationException.class, () -> Session.builder()
            .login(null)
            .sessionToken(new SessionToken(UUID.randomUUID().toString()))
            .expiresAt(new ExpiresAtTimestamp(LocalDateTime.now().plusHours(8)))
            .build());
    }

    @Test
    void shouldThrowException_whenSessionTokenIsNull() {
        assertThrows(ValidationException.class, () -> Session.builder()
            .login(activeLogin)
            .expiresAt(new ExpiresAtTimestamp(LocalDateTime.now().plusHours(8)))
            .build());
    }

    @Test
    void shouldThrowException_whenExpiresAtIsNull() {
        assertThrows(ValidationException.class, () -> Session.builder()
            .login(activeLogin)
            .sessionToken(new SessionToken(UUID.randomUUID().toString()))
            .build());
    }

    // NEW: role is now a required field on the builder (see
    // Session.Builder.build()) — same reasoning as the three
    // pre-existing null-field tests above.
    @Test
    void shouldThrowException_whenRoleIsNull() {
        assertThrows(ValidationException.class, () -> Session.builder()
            .login(activeLogin)
            .sessionToken(new SessionToken(UUID.randomUUID().toString()))
            .expiresAt(new ExpiresAtTimestamp(LocalDateTime.now().plusHours(8)))
            .build());
    }

    // ── helper ─────────────────────────────────────────────────────────────────

    // CHANGED: role added. Fixed to INSTRUCTOR so every caller of this
    // helper gets a deterministic value to assert against (see
    // shouldReturnNewInstance_whenRoleIsChanged above).
    private Session buildSession(Login login, LocalDateTime expiresAt) {
        return Session.builder()
            .login(login)
            .sessionToken(new SessionToken(UUID.randomUUID().toString()))
            .expiresAt(ExpiresAtTimestamp.of(expiresAt))
            .role(UserRole.INSTRUCTOR)
            .build();
    }
}