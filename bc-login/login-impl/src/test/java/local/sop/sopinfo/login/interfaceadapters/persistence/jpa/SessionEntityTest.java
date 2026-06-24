package local.sop.sopinfo.login.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import local.sop.sopinfo.sharedkernel.enums.LoginStatus;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

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

        SessionEntity entity = SessionEntity.builder()
            .id(UUID.randomUUID())
            .login(persistedLogin)
            .sessionToken(token)
            .expiresAt(expiresAt)
            .build();
        entityManager.persist(entity);
        entityManager.flush();

        SessionEntity found = entityManager.find(SessionEntity.class, entity.getId());

        assertNotNull(found);
        assertEquals(persistedLogin.getId(), found.getLogin().getId());
        assertEquals(token, found.getSessionToken());
        assertNotNull(found.getCreatedAt());
        assertEquals(expiresAt, found.getExpiresAt());
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

    // ── isExpired ──────────────────────────────────────────────────────────────

    @Test
    void shouldReturnTrue_whenSessionIsExpired() {
        SessionEntity expired = SessionEntity.builder()
            .id(UUID.randomUUID())
            .login(persistedLogin)
            .sessionToken(UUID.randomUUID().toString())
            .expiresAt(LocalDateTime.now().minusHours(1))
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

    // ── helper ─────────────────────────────────────────────────────────────────

    private SessionEntity buildSession() {
        return SessionEntity.builder()
            .id(UUID.randomUUID())
            .login(persistedLogin)
            .sessionToken(UUID.randomUUID().toString())
            .expiresAt(LocalDateTime.now().plusHours(8))
            .build();
    }
}