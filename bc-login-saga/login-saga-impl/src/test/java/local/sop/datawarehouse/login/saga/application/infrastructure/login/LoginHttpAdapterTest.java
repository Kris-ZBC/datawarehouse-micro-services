package local.sop.datawarehouse.login.saga.application.infrastructure.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import local.sop.datawarehouse.login.saga.application.api.dto.LoginResult;
import local.sop.datawarehouse.login.saga.application.infrastructure.request.PayloadAuthenticate;
import local.sop.datawarehouse.login.saga.application.infrastructure.request.PayloadCreateSession;
import local.sop.datawarehouse.login.saga.application.infrastructure.request.PayloadLogout;
import local.sop.datawarehouse.login.saga.application.ports.out.login.LoginPort.AuthenticationResult;
import local.sop.datawarehouse.sharedlib.enums.UserRole;

// CHANGED: was testing login()/compensate() — neither exists on
// LoginHttpAdapter anymore (split into authenticate()/createSession(),
// compensate() replaced by logout() — see LoginPort's own Javadoc).
@ExtendWith(MockitoExtension.class)
class LoginHttpAdapterTest {

    @Mock(name = "login")
    private RestClient loginClient;

    private LoginHttpAdapter loginAdapter;

    @BeforeEach
    void setUp() {
        loginAdapter = new LoginHttpAdapter(loginClient);
    }

    @Test
    void authenticate_shouldReturnAuthenticationResultFromDownstream() {
        AuthenticationResult expected =
            new AuthenticationResult(UUID.randomUUID(), UUID.randomUUID(), "testuser");
        PayloadAuthenticate request = new PayloadAuthenticate("testuser", "testpass");

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(loginClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/internal/logins/sessions/authenticate")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(PayloadAuthenticate.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(AuthenticationResult.class)).thenReturn(expected);

        AuthenticationResult result = loginAdapter.authenticate(request.username(), request.password());

        assertEquals(expected, result);
        verify(loginClient).post();
        verify(requestBodyUriSpec).uri("/internal/logins/sessions/authenticate");
        verify(requestBodySpec).body(eq(request));
        verify(responseSpec).body(AuthenticationResult.class);
    }

    @Test
    void createSession_shouldReturnLoginResultFromDownstream() {
        UUID loginId = UUID.randomUUID();
        LoginResult expected = new LoginResult(loginId, UUID.randomUUID(), "testuser",
            "INSTRUCTOR", "test-token", LocalDateTime.now(), LocalDateTime.now().plusHours(8));
        PayloadCreateSession request = new PayloadCreateSession(loginId, UserRole.INSTRUCTOR);

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(loginClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/internal/logins/sessions")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(PayloadCreateSession.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(LoginResult.class)).thenReturn(expected);

        LoginResult result = loginAdapter.createSession(loginId, UserRole.INSTRUCTOR);

        assertEquals(expected, result);
        verify(loginClient).post();
        verify(requestBodyUriSpec).uri("/internal/logins/sessions");
        verify(requestBodySpec).body(eq(request));
        verify(responseSpec).body(LoginResult.class);
    }

    @Test
    void logout_shouldPostToLogoutEndpoint() {
        String sessionToken = UUID.randomUUID().toString();
        PayloadLogout request = new PayloadLogout(sessionToken);

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(loginClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/internal/logins/sessions/logout")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(PayloadLogout.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        loginAdapter.logout(sessionToken);

        verify(loginClient).post();
        verify(requestBodyUriSpec).uri("/internal/logins/sessions/logout");
        verify(requestBodySpec).body(eq(request));
        verify(responseSpec).toBodilessEntity();
    }
}