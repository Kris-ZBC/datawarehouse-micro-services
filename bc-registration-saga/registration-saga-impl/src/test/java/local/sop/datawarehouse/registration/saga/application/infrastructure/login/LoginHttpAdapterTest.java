package local.sop.datawarehouse.registration.saga.application.infrastructure.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.registration.saga.application.api.dto.login.CreateLoginCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.login.LoginResponse;
import local.sop.datawarehouse.registration.saga.application.infrastructure.login.LoginHttpAdapter.PayloadUpdateLoginStatus;
import local.sop.datawarehouse.registration.saga.application.infrastructure.response.ResponseLoginCreated;

@ExtendWith(MockitoExtension.class)
class LoginHttpAdapterTest {
        /*
     * ----------------------------------------------------------- *
     * LoginHttpAdapter tests
     * -----------------------------------------------------------
     */

    @Mock(name = "login")
    private RestClient loginClient;

    @Mock
    RestClient.RequestHeadersUriSpec<?> mockRequestHeadersUriSpec;

    private LoginHttpAdapter loginAdapter;

    @BeforeEach
    void setUp() {
        loginAdapter = new LoginHttpAdapter(loginClient);
    }

    @Test
    void login_create_shouldReturnUuidFromDownstream() {
        UUID expectedId = UUID.randomUUID();
        var request = new CreateLoginCmd(expectedId,
                "john.doe@example.com", "active" );

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(loginClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/logins")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(CreateLoginCmd.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseLoginCreated.class)).thenReturn(new ResponseLoginCreated(expectedId, "Kode1234!", null));

        ResponseLoginCreated result = loginAdapter.create(request);
        assertEquals(expectedId, result.id());
        verify(loginClient).post();
        verify(mockRequestBodyUriSpec).uri("/internal/logins");
        verify(mockRequestBodySpec).body(request);
        verify(mockResponseSpec).body(ResponseLoginCreated.class);
    }

    @Test
    void login_compensate_shouldReturnResultFromDownstream() {
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        var request = new PayloadCompensateCreate(LoginHttpAdapter.class, state);
        var expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(loginClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/logins/{id}/compensate/create", id))
                .thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadCompensateCreate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

        ResponseCompensated result = loginAdapter.compensate(id, LoginHttpAdapter.class, state);

        assertEquals(expectedResult.sagaState(), result.sagaState());
        assertEquals(expectedResult.success(), result.success());
        verify(loginClient).put();
        verify(mockRequestBodyUriSpec).uri("/internal/logins/{id}/compensate/create", id);
        verify(mockRequestBodySpec).body(request);
        verify(mockResponseSpec).body(ResponseCompensated.class);
    }

    @Test
    void login_get_shouldReturnLoginFromDownstream() {
        UUID id = UUID.randomUUID();
        var expected = new LoginResponse(id, UUID.randomUUID(), "john.doe@example.com", "active");
        var mockRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        doReturn(mockRequestHeadersUriSpec).when(loginClient).get();
        doReturn(mockRequestHeadersSpec).when(mockRequestHeadersUriSpec)
                .uri("/internal/logins/{id}", id);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(LoginResponse.class)).thenReturn(expected);

        LoginResponse result = loginAdapter.getById(id);

        assertEquals(expected, result);
        verify(loginClient).get();
        verify(mockRequestHeadersUriSpec).uri("/internal/logins/{id}", id);
        verify(mockResponseSpec).body(LoginResponse.class);
    }

    // NEW: coverage for disableLogin() — added so registration-saga can
    // disable the tech user once an instructor is confirmed to exist.
    // PayloadUpdateLoginStatus is a private record nested inside
    // LoginHttpAdapter, so it can't be constructed here to match
    // exactly — verifying the call happened with any() body is the
    // most this test can assert from outside the class.
    @Test
    void disableLogin_shouldPostToStatusEndpoint() {
        UUID loginId = UUID.randomUUID();

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(loginClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/logins/status")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadUpdateLoginStatus.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);

        loginAdapter.disableLogin(loginId);

        verify(loginClient).post();
        verify(mockRequestBodyUriSpec).uri("/internal/logins/status");
        verify(mockRequestBodySpec).body(any(PayloadUpdateLoginStatus.class));
        verify(mockResponseSpec).toBodilessEntity();
    }
}