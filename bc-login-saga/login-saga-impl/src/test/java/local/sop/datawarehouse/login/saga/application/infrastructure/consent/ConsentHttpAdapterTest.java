package local.sop.datawarehouse.login.saga.application.infrastructure.consent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import local.sop.datawarehouse.login.saga.application.infrastructure.request.PayloadCompensate;
import local.sop.datawarehouse.login.saga.application.infrastructure.response.ResponseCompensated;

@ExtendWith(MockitoExtension.class)
class ConsentHttpAdapterTest {

    @Mock(name = "consent")
    private RestClient consentClient;

    private ConsentHttpAdapter consentAdapter;

    @BeforeEach
    void setUp() {
        consentAdapter = new ConsentHttpAdapter(consentClient);
    }

    @Test
    void compensate_shouldReturnCompensationResultFromDownstream() {
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        PayloadCompensate request = new PayloadCompensate(id, ConsentHttpAdapter.class, state);
        ResponseCompensated expected = new ResponseCompensated(state, true);

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(consentClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/internal/consents/compensate")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(PayloadCompensate.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ResponseCompensated.class)).thenReturn(expected);

        ResponseCompensated result = consentAdapter.compensate(id, ConsentHttpAdapter.class, state);

        assertEquals(expected.sagaState(), result.sagaState());
        assertEquals(expected.success(), result.success());
        verify(consentClient).post();
        verify(requestBodyUriSpec).uri("/internal/consents/compensate");
        verify(requestBodySpec).body(eq(request));
        verify(responseSpec).body(ResponseCompensated.class);
    }
}
