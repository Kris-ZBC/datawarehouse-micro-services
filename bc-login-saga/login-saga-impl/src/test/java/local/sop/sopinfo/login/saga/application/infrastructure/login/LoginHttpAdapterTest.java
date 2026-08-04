package local.sop.sopinfo.login.saga.application.infrastructure.login;

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

import local.sop.sopinfo.login.saga.application.api.dto.LoginResult;
import local.sop.sopinfo.login.saga.application.infrastructure.request.PayloadCompensate;
import local.sop.sopinfo.login.saga.application.infrastructure.request.PayloadLogin;
import local.sop.sopinfo.login.saga.application.infrastructure.response.ResponseCompensated;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

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
    void login_shouldReturnLoginResultFromDownstream() {
        UUID loginId = UUID.randomUUID();
        LoginResult expected = new LoginResult(loginId, UUID.randomUUID(), "testuser",
            "test-token", LocalDateTime.now(), LocalDateTime.now().plusHours(8));
        PayloadLogin request = new PayloadLogin("testuser", "testpass");

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(loginClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/internal/logins/sessions/login")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(PayloadLogin.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(LoginResult.class)).thenReturn(expected);

        LoginResult result = loginAdapter.login(request.username(), request.password());

        assertEquals(expected, result);
        verify(loginClient).post();
        verify(requestBodyUriSpec).uri("/internal/logins/sessions/login");
        verify(requestBodySpec).body(eq(request));
        verify(responseSpec).body(LoginResult.class);
    }

    @Test
    void compensate_shouldReturnCompensationResultFromDownstream() {
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        PayloadCompensate request = new PayloadCompensate(id, LoginHttpAdapter.class, state);
        ResponseCompensated expected = new ResponseCompensated(state, true);

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(loginClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/internal/logins/compensate")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(PayloadCompensate.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ResponseCompensated.class)).thenReturn(expected);

        ResponseCompensated result = loginAdapter.compensate(id, LoginHttpAdapter.class, state);

        assertEquals(expected.sagaState(), result.sagaState());
        assertEquals(expected.success(), result.success());
        verify(loginClient).post();
        verify(requestBodyUriSpec).uri("/internal/logins/compensate");
        verify(requestBodySpec).body(eq(request));
        verify(responseSpec).body(ResponseCompensated.class);
    }
}
