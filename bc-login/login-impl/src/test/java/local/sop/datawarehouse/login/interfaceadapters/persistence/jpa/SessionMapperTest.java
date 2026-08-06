package local.sop.datawarehouse.login.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.enums.LoginStatus;
import local.sop.datawarehouse.login.domain.model.Login;
import local.sop.datawarehouse.login.domain.model.Session;
import local.sop.datawarehouse.login.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.datawarehouse.login.domain.model.valueobjects.ExpiresAtTimestamp;
import local.sop.datawarehouse.login.domain.model.valueobjects.HashedPassword;
import local.sop.datawarehouse.login.domain.model.valueobjects.PersonRef;
import local.sop.datawarehouse.login.domain.model.valueobjects.SessionToken;
import local.sop.datawarehouse.login.domain.model.valueobjects.Username;

class SessionMapperTest {

    private static final String VALID_HASH = "$2b$10$" + "a".repeat(53);

    private final SessionMapper mapper = new SessionMapper();

    private LoginEntity loginEntity;
    private Login login;

    @BeforeEach
    void setUp() {
        UUID loginId = UUID.randomUUID();

        loginEntity = LoginEntity.builder()
            .id(loginId)
            .personRef(UUID.randomUUID())
            .username("nick579a@zbc.dk")
            .password(VALID_HASH)
            .status(LoginStatus.ACTIVATED)
            .build();

        login = Login.builder()
            .id(new local.sop.datawarehouse.login.domain.model.valueobjects.LoginId(loginId))
            .personRef(new PersonRef(loginEntity.getPersonRef()))
            .username(new Username(loginEntity.getUsername()))
            .password(HashedPassword.of(VALID_HASH))
            .status(LoginStatus.ACTIVATED)
            .build();
    }

    // ── toDomain ───────────────────────────────────────────────────────────────

    @Test
    void shouldMapToDomain() {
        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(30);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(8);

        SessionEntity entity = SessionEntity.builder()
            .id(UUID.randomUUID())
            .login(loginEntity)
            .sessionToken(UUID.randomUUID().toString())
            .createdAt(createdAt)
            .expiresAt(expiresAt)
            .build();

        Session session = mapper.toDomain(entity);

        assertNotNull(session);
        assertEquals(entity.getId(), session.getId().value());
        assertEquals(entity.getLogin().getId(), session.getLogin().getId().value());
        assertEquals(entity.getSessionToken(), session.getToken().value());
        assertEquals(createdAt, session.getCreatedAt().value());
        assertEquals(expiresAt, session.getExpiresAt().value());

        // verify the full Login aggregate is reconstructed correctly
        assertNotNull(session.getLogin());
        assertEquals(loginEntity.getId(), session.getLogin().getId().value());
        assertEquals(loginEntity.getUsername(), session.getLogin().getUsername().value());
    }

    // ── toEntity ───────────────────────────────────────────────────────────────

    @Test
    void shouldMapToEntity() {
        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(30);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(8);

        Session session = Session.builder()
            .login(login)
            .sessionToken(new SessionToken(UUID.randomUUID().toString()))
            .createdAt(new CreatedAtTimestamp(createdAt))
            .expiresAt(new ExpiresAtTimestamp(expiresAt))
            .build();

        SessionEntity entity = mapper.toEntity(session, loginEntity);

        assertNotNull(entity);
        assertEquals(session.getId().value(), entity.getId());
        assertEquals(login.getId().value(), entity.getLogin().getId());
        assertEquals(session.getToken().value(), entity.getSessionToken());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(expiresAt, entity.getExpiresAt());

        // verify the LoginEntity reference is set correctly
        assertNotNull(entity.getLogin());
        assertEquals(loginEntity.getId(), entity.getLogin().getId());
    }
}