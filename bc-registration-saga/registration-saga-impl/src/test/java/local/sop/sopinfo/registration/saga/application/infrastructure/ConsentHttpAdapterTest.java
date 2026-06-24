package local.sop.sopinfo.registration.saga.application.infrastructure;

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

import local.sop.sopinfo.registration.saga.application.api.dto.consent.ConsentResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.consent.ConsentStatementResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.consent.GrantConsentCmd;
import local.sop.sopinfo.registration.saga.application.infrastructure.consent.ConsentHttpAdapter;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;

@ExtendWith(MockitoExtension.class)
 class ConsentHttpAdapterTest {
    
     /*
     * ----------------------------------------------------------- *
     * ConsentHttpAdapterTest tests
     * -----------------------------------------------------------
     */

    @Mock(name = "consent")
    private RestClient consentClient;

    @Mock
    RestClient.RequestHeadersUriSpec<?> mockRequestHeadersUriSpec;

    private ConsentHttpAdapter consentAdapter;

    @BeforeEach
    void setUp() {
        consentAdapter = new ConsentHttpAdapter(consentClient);
    }

    @Test
    void consent_get_shouldReturnConsentFromDownstream() {
        UUID id = UUID.randomUUID();
        var expected = new ConsentResponse(id, UUID.randomUUID(), "ACTIVE", UUID.randomUUID(), "consentStatement", "consentPurpose", "constentType", true);
        var mockRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        doReturn(mockRequestHeadersUriSpec).when(consentClient).get();
        doReturn(mockRequestHeadersSpec).when(mockRequestHeadersUriSpec)
                .uri("/internal/consents/consent/{id}", id);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ConsentResponse.class)).thenReturn(expected);

        ConsentResponse result = consentAdapter.getById(id);

        assertEquals(expected, result);
        verify(consentClient).get();
        verify(mockRequestHeadersUriSpec).uri("/internal/consents/consent/{id}", id);
        verify(mockResponseSpec).body(ConsentResponse.class);
    }

    
    @Test
    void consentStatement_get_shouldReturnConsentFromDownstream() {
        UUID id = UUID.randomUUID();
        var expected = new ConsentStatementResponse(id, "statement", true);
        var mockRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        doReturn(mockRequestHeadersUriSpec).when(consentClient).get();
        doReturn(mockRequestHeadersSpec).when(mockRequestHeadersUriSpec)
                .uri("/internal/consent/statements/statements?id={id}", id);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ConsentStatementResponse.class)).thenReturn(expected);

        ConsentStatementResponse result = consentAdapter.getConsentStatementById(id);

        assertEquals(expected, result);
        verify(consentClient).get();
        verify(mockRequestHeadersUriSpec).uri("/internal/consent/statements/statements?id={id}", id);
        verify(mockResponseSpec).body(ConsentStatementResponse.class);
    }

    @Test
    void consent_compensate_shouldReturnResultFromDownstream() {
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        var request = new PayloadCompensateCreate(ConsentHttpAdapter.class, state);
        var expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(consentClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/consent/statements/{id}/compensate/create", id))
                .thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadCompensateCreate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

        ResponseCompensated result = consentAdapter.compensate(id, ConsentHttpAdapter.class, state);

        assertEquals(expectedResult.sagaState(), result.sagaState());
        assertEquals(expectedResult.success(), result.success());
        verify(consentClient).put();
        verify(mockRequestBodyUriSpec).uri("/internal/consent/statements/{id}/compensate/create", id);
        verify(mockRequestBodySpec).body(request);
        verify(mockResponseSpec).body(ResponseCompensated.class);
    }
    @Test
    void consent_grant_shouldReturnUuidFromDownstream() {
        UUID expectedId = UUID.randomUUID();
        var request = new GrantConsentCmd(UUID.randomUUID(), UUID.randomUUID(), null, null, null );

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(consentClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/consents/consent/grant")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(GrantConsentCmd.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(UUID.class)).thenReturn(expectedId);

        UUID result = consentAdapter.grant(request);
        assertEquals(expectedId, result);
        verify(consentClient).post();
        verify(mockRequestBodyUriSpec).uri("/internal/consents/consent/grant");
        verify(mockRequestBodySpec).body(request);
        verify(mockResponseSpec).body(UUID.class);
    }
}
