package local.sop.datawarehouse.login.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.datawarehouse.login.domain.model.Session;
import local.sop.datawarehouse.login.domain.model.valueobjects.SessionToken;

@ExtendWith(MockitoExtension.class)
class SessionRepositoryAdapterTest {

    @Mock
    private SessionSpringDataRepository sessionRepo;

	@Mock
	private LoginSpringDataRepository loginRepo;

    @Mock
    private SessionMapper mapper;

    private SessionRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SessionRepositoryAdapter(mapper, sessionRepo, loginRepo);
    }

    // ── save ───────────────────────────────────────────────────────────────────

	@Test
	void shouldSaveSession() {
		Session session = mock(Session.class);
		SessionEntity entity = mock(SessionEntity.class);
		LoginEntity loginEntity = mock(LoginEntity.class);
		local.sop.datawarehouse.login.domain.model.Login login = mock(
			local.sop.datawarehouse.login.domain.model.Login.class);
		local.sop.datawarehouse.login.domain.model.valueobjects.LoginId loginId =
			new local.sop.datawarehouse.login.domain.model.valueobjects.LoginId(UUID.randomUUID());
		SessionToken sessionToken = new SessionToken(UUID.randomUUID().toString());

		when(session.getLogin()).thenReturn(login);
		 when(session.getToken()).thenReturn(sessionToken); 
		when(login.getId()).thenReturn(loginId);
		when(loginRepo.findById(loginId.value())).thenReturn(Optional.of(loginEntity));
		when(mapper.toEntity(session, loginEntity)).thenReturn(entity);

		adapter.save(session);

		verify(loginRepo).findById(loginId.value());
		verify(mapper).toEntity(session, loginEntity);
		verify(sessionRepo).save(entity);
	}

    // ── findBySessionToken ─────────────────────────────────────────────────────

    @Test
    void shouldReturnSession_whenTokenExists() {
        SessionToken token  = new SessionToken(UUID.randomUUID().toString());
        SessionEntity entity = mock(SessionEntity.class);
        Session session      = mock(Session.class);

        when(sessionRepo.findBySessionToken(token.value())).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(session);

        Optional<Session> result = adapter.findBySessionToken(token);

        assertTrue(result.isPresent());
        assertEquals(session, result.get());
        verify(sessionRepo).findBySessionToken(token.value());
        verify(mapper).toDomain(entity);
        verifyNoMoreInteractions(sessionRepo, mapper);
    }

    @Test
    void shouldReturnEmpty_whenTokenNotFound() {
        SessionToken token = new SessionToken(UUID.randomUUID().toString());

        when(sessionRepo.findBySessionToken(token.value())).thenReturn(Optional.empty());

        Optional<Session> result = adapter.findBySessionToken(token);

        assertTrue(result.isEmpty());
        verify(sessionRepo).findBySessionToken(token.value());
        verifyNoMoreInteractions(sessionRepo, mapper);
    }

    // ── deleteBySessionToken ───────────────────────────────────────────────────

    @Test
    void shouldDeleteSession_whenTokenExists() {
        String token = UUID.randomUUID().toString();
        when(sessionRepo.deleteBySessionToken(token)).thenReturn(1);

        boolean result = adapter.deleteBySessionToken(new SessionToken(token));

        assertTrue(result);
        verify(sessionRepo).deleteBySessionToken(token);
        verify(sessionRepo, never()).findBySessionToken(any());
    }

    @Test
    void shouldNotDelete_whenTokenNotFound() {
        String token = UUID.randomUUID().toString();
        when(sessionRepo.deleteBySessionToken(token)).thenReturn(0);

        boolean result = adapter.deleteBySessionToken(new SessionToken(token));

        assertFalse(result);
        verify(sessionRepo).deleteBySessionToken(token);
        verify(sessionRepo, never()).findBySessionToken(any());
    }

	@Test
void shouldThrowValidationException_whenLoginNotFoundDuringSave() {
    Session session = mock(Session.class);
    local.sop.datawarehouse.login.domain.model.Login login = mock(
        local.sop.datawarehouse.login.domain.model.Login.class);
    local.sop.datawarehouse.login.domain.model.valueobjects.LoginId loginId =
        new local.sop.datawarehouse.login.domain.model.valueobjects.LoginId(UUID.randomUUID());

    when(session.getLogin()).thenReturn(login);
    when(login.getId()).thenReturn(loginId);
    when(loginRepo.findById(loginId.value())).thenReturn(Optional.empty());

    assertThrows(local.sop.common.libs.sharedkernel.exceptions.ValidationException.class,
        () -> adapter.save(session));

    verify(sessionRepo, never()).save(any());
}
}