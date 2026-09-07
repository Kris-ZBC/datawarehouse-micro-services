package local.sop.datawarehouse.gateway.common.handlers.login.application.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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

import local.sop.datawarehouse.gateway.common.handlers.login.application.ports.LoginPort.LoginResult;
 
@ExtendWith(MockitoExtension.class)
class LoginAdapterTest {
 
    @Mock(name = "login-saga")
    private RestClient loginSagaClient;
 
    @Mock(name = "login")
    private RestClient loginClient;
 
    private LoginAdapter adapter;
 
    @BeforeEach
    void setUp() {
        adapter = new LoginAdapter(loginSagaClient, loginClient);
    }
 
    @Test
    void login_shouldPostToLoginSaga_andReturnResult() {
        LoginResult expected = new LoginResult(UUID.randomUUID(), UUID.randomUUID(), "nick",
                "INSTRUCTOR", "token-123", LocalDateTime.now(), LocalDateTime.now().plusHours(8));
 
        var uriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var bodySpec = mock(RestClient.RequestBodySpec.class);
        var responseSpec = mock(RestClient.ResponseSpec.class);
 
        when(loginSagaClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri("/internal/saga/logins/sessions/login")).thenReturn(bodySpec);
        when(bodySpec.body(any(Object.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(LoginResult.class)).thenReturn(expected);
 
        LoginResult result = adapter.login("nick", "pw");
 
        assertEquals(expected, result);
        verify(loginSagaClient).post();
        verify(uriSpec).uri("/internal/saga/logins/sessions/login");
    }
 
    @Test
    void logout_shouldPostDirectlyToBcLogin_notThroughSaga() {
        var uriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var bodySpec = mock(RestClient.RequestBodySpec.class);
        var responseSpec = mock(RestClient.ResponseSpec.class);
 
        when(loginClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri("/internal/logins/sessions/logout")).thenReturn(bodySpec);
        when(bodySpec.body(any(Object.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
 
        adapter.logout("token-123");
 
        verify(loginClient).post();
        verify(uriSpec).uri("/internal/logins/sessions/logout");
        verify(responseSpec).toBodilessEntity();
        org.mockito.Mockito.verifyNoInteractions(loginSagaClient);
    }
}
