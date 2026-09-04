package local.sop.datawarehouse.login.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.sharedlib.enums.LoginStatus;
import local.sop.datawarehouse.sharedlib.enums.UserRole;

@DataJpaTest
@ActiveProfiles({"test", "h2"})
@Transactional
class SessionEntityTest {

    @Autowired
    private EntityManager entityManager;

    private LoginEntity persistedLogin;

    @BeforeEach
    void setUp() {
        persistedLogin = LoginEntity.builder()
            .id(UUID.randomUUID())
            .personRef(UUID.randomUUID())
            .username("nick579a@zbc.dk")
            .password("$2b$10$" + "a".repeat(53))
            .status(LoginStatus.ACTIVATED)
            .build();
        entityManager.persist(persistedLogin);
        entityManager.flush();
    }

    // ── persistence ────────────────────────────────────────────────────────────

    @Test
    void shouldPersistAndRetrieveSession() {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(8);

        // CHANGED: role added. The role column is NOT NULL at the DB
        // level — without this, flush() below would fail with a
        // DataIntegrityViolationException, not the builder (the
        // builder's own validation only fires if .role() is called
        // with an explicit null, not if it's never called at all).
        SessionEntity entity = SessionEntity.builder()
            .id(UUID.randomUUID())
            .login(persistedLogin)
            .sessionToken(token)
            .expiresAt(expiresAt)
            .role(UserRole.INSTRUCTOR)
            .build();
        entityManager.persist(entity);
        entityManager.flush();

        SessionEntity found = entityManager.find(SessionEntity.class, entity.getId());

        assertNotNull(found);
        assertEquals(persistedLogin.getId(), found.getLogin().getId());
        assertEquals(token, found.getSessionToken());
        assertNotNull(found.getCreatedAt());
        assertEquals(expiresAt, found.getExpiresAt());
        assertEquals(UserRole.INSTRUCTOR, found.getRole());
    }

    // NEW: confirms the NOT NULL column constraint actually catches
    // what the builder alone doesn't — a SessionEntity built without
    // ever calling .role() persists fine in memory (role stays null)
    // but must fail at the database on flush.
    @Test
    void shouldFailToPersist_whenRoleWasNeverSet() {
        SessionEntity entity = SessionEntity.builder()
            .id(UUID.randomUUID())
            .login(persistedLogin)
            .sessionToken(UUID.randomUUID().toString())
            .expiresAt(LocalDateTime.now().plusHours(8))
            .build();

        assertThrows(ConstraintViolationException.class, () -> { //NOT A DataIntegrityViolationException, because the latter is a Spring wrapper around the former
            entityManager.persist(entity);
            entityManager.flush();
        });
    }

    // ── with-methods ───────────────────────────────────────────────────────────

    @Test
    void shouldReturnNewInstance_whenLoginChanged() {
        SessionEntity original = buildSession();

        LoginEntity newLogin = LoginEntity.builder()
            .id(UUID.randomUUID())
            .personRef(UUID.randomUUID())
            .username("john.doe@zbc.dk")
            .password("$2b$10$" + "b".repeat(53))
            .status(LoginStatus.ACTIVATED)
            .build();

        SessionEntity updated = original.withLogin(newLogin);

        assertNotSame(original, updated);
        assertEquals(newLogin.getId(), updated.getLogin().getId());
        assertEquals(persistedLogin.getId(), original.getLogin().getId());
        assertEquals(original.getId(), updated.getId());
        assertEquals(original.getSessionToken(), updated.getSessionToken());
    }

    @Test
    void shouldReturnNewInstance_whenSessionTokenChanged() {
        SessionEntity original = buildSession();
        String newToken = UUID.randomUUID().toString();

        SessionEntity updated = original.withSessionToken(newToken);

        assertNotSame(original, updated);
        assertEquals(newToken, updated.getSessionToken());
        assertEquals(original.getId(), updated.getId());
        assertEquals(original.getLogin().getId(), updated.getLogin().getId());
    }

    @Test
    void shouldReturnNewInstance_whenExpiresAtChanged() {
        SessionEntity original = buildSession();
        LocalDateTime newExpiresAt = LocalDateTime.now().plusHours(16);

        SessionEntity updated = original.withExpiresAt(newExpiresAt);

        assertNotSame(original, updated);
        assertEquals(newExpiresAt, updated.getExpiresAt());
        assertEquals(original.getId(), updated.getId());
        assertEquals(original.getLogin().getId(), updated.getLogin().getId());
    }

    // NEW: mirrors the other with-method tests, for the role field
    // added alongside the SSO rework.
    @Test
    void shouldReturnNewInstance_whenRoleChanged() {
        SessionEntity original = buildSession();

        SessionEntity updated = original.withRole(UserRole.APPRENTICE);

        assertNotSame(original, updated);
        assertEquals(UserRole.APPRENTICE, updated.getRole());
        assertEquals(UserRole.INSTRUCTOR, original.getRole());
        assertEquals(original.getId(), updated.getId());
    }

    // ── isExpired ──────────────────────────────────────────────────────────────

    @Test
    void shouldReturnTrue_whenSessionIsExpired() {
        SessionEntity expired = SessionEntity.builder()
            .id(UUID.randomUUID())
            .login(persistedLogin)
            .sessionToken(UUID.randomUUID().toString())
            .expiresAt(LocalDateTime.now().minusHours(1))
            .role(UserRole.INSTRUCTOR)
            .build();

        assertTrue(expired.isExpired());
    }

    @Test
    void shouldReturnFalse_whenSessionIsNotExpired() {
        SessionEntity valid = SessionEntity.builder()
            .id(UUID.randomUUID())
            .login(persistedLogin)
            .sessionToken(UUID.randomUUID().toString())
            .expiresAt(LocalDateTime.now().plusHours(1))
            .role(UserRole.INSTRUCTOR)
            .build();

        assertFalse(valid.isExpired());
    }

    // ── builder validation ─────────────────────────────────────────────────────

    @Test
    void shouldThrowException_whenIdIsNull() {
        assertThrows(ValidationException.class, () ->
            SessionEntity.builder()
                .id(null)
                .login(persistedLogin)
                .sessionToken(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusHours(8))
                .build());
    }

    @Test
    void shouldThrowException_whenLoginIsNull() {
        assertThrows(ValidationException.class, () ->
            SessionEntity.builder()
                .id(UUID.randomUUID())
                .login(null)
                .sessionToken(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusHours(8))
                .build());
    }

    @Test
    void shouldThrowException_whenSessionTokenIsNull() {
        assertThrows(ValidationException.class, () ->
            SessionEntity.builder()
                .id(UUID.randomUUID())
                .login(persistedLogin)
                .sessionToken(null)
                .expiresAt(LocalDateTime.now().plusHours(8))
                .build());
    }

    @Test
    void shouldThrowException_whenSessionTokenIsBlank() {
        assertThrows(ValidationException.class, () ->
            SessionEntity.builder()
                .id(UUID.randomUUID())
                .login(persistedLogin)
                .sessionToken("   ")
                .expiresAt(LocalDateTime.now().plusHours(8))
                .build());
    }

    @Test
    void shouldThrowException_whenExpiresAtIsNull() {
        assertThrows(ValidationException.class, () ->
            SessionEntity.builder()
                .id(UUID.randomUUID())
                .login(persistedLogin)
                .sessionToken(UUID.randomUUID().toString())
                .expiresAt(null)
                .build());
    }

    // NEW: unlike the other builder-validation tests above, role's
    // validation only fires when .role() is explicitly called with
    // null — an unset role passes the builder silently (see
    // shouldFailToPersist_whenRoleWasNeverSet above for that case).
    // This test covers the explicit-null path.
    @Test
    void shouldThrowException_whenRoleIsExplicitlyNull() {
        assertThrows(ValidationException.class, () ->
            SessionEntity.builder()
                .id(UUID.randomUUID())
                .login(persistedLogin)
                .sessionToken(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusHours(8))
                .role(null)
                .build());
    }

    // ── helper ─────────────────────────────────────────────────────────────────

    // CHANGED: role added, fixed to INSTRUCTOR for deterministic
    // assertions in tests that build on top of this helper.
    private SessionEntity buildSession() {
        return SessionEntity.builder()
            .id(UUID.randomUUID())
            .login(persistedLogin)
            .sessionToken(UUID.randomUUID().toString())
            .expiresAt(LocalDateTime.now().plusHours(8))
            .role(UserRole.INSTRUCTOR)
            .build();
    }
}