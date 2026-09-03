package local.sop.datawarehouse.registration.saga.application.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.sharedlib.enums.ConsentPurpose;
import local.sop.datawarehouse.sharedlib.enums.ConsentStatus;
import local.sop.datawarehouse.sharedlib.enums.ConsentType;
import local.sop.datawarehouse.registration.saga.application.api.dto.consent.ConsentResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.consent.GrantConsentCmd;
import local.sop.datawarehouse.registration.saga.application.infrastructure.consentsaga.ConsentSagaHttpAdapter;

/**
 * Tests ConsentSagaHttpAdapter — the consent-saga-routed WRITE path
 * (grant / compensateConsent). See ConsentHttpAdapterTest for the
 * direct-to-bc-consent read path.
 */
@ExtendWith(MockitoExtension.class)
class ConsentSagaHttpAdapterTest {

    @Mock(name = "consent-saga")
    private RestClient consentSagaClient;

    private ConsentSagaHttpAdapter consentSagaAdapter;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        consentSagaAdapter = new ConsentSagaHttpAdapter(consentSagaClient);
    }

    @Test
    void consent_compensate_shouldReturnResultFromDownstream() {
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        var request = new PayloadCompensateCreate(ConsentSagaHttpAdapter.class, state);
        var expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        // The adapter's compensateConsent() calls .post(), not .put().
        when(consentSagaClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/saga/consents/consent/{id}/compensate/create", id))
                .thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadCompensateCreate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

        ResponseCompensated result = consentSagaAdapter.compensateConsent(id, ConsentSagaHttpAdapter.class, state);

        assertEquals(expectedResult.sagaState(), result.sagaState());
        assertEquals(expectedResult.success(), result.success());
        verify(consentSagaClient).post();
        verify(mockRequestBodyUriSpec).uri("/internal/saga/consents/consent/{id}/compensate/create", id);
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(ResponseCompensated.class);
    }

    @Test
    void consent_grant_shouldReturnConsentResponseFromDownstream() {
        UUID sessionId = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();
        UUID consentStatementRef = UUID.randomUUID();
        var request = new GrantConsentCmd(sessionId, personRef, consentStatementRef, ConsentStatus.ACTIVE,
                UUID.randomUUID(), local.sop.datawarehouse.sharedlib.enums.ActorType.USER,
                local.sop.datawarehouse.sharedlib.enums.Severity.INFO,
                "originSystem", "originService", "originComponent", "data", "description");

        // grant() returns ConsentResponse, matching consent-saga's
        // actual ResponseEntity<ConsentResponse> shape.
        var expected = new ConsentResponse(
                UUID.randomUUID(), personRef,"ACTIVE", consentStatementRef, "Statement text", ConsentPurpose.RESEARCH, ConsentType.ONE_TIME, true);

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(consentSagaClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/saga/consents/consent/grant")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(GrantConsentCmd.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ConsentResponse.class)).thenReturn(expected);

        ConsentResponse result = consentSagaAdapter.grant(request);

        assertEquals(expected, result);
        verify(consentSagaClient).post();
        verify(mockRequestBodyUriSpec).uri("/internal/saga/consents/consent/grant");
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(ConsentResponse.class);
    }
}
