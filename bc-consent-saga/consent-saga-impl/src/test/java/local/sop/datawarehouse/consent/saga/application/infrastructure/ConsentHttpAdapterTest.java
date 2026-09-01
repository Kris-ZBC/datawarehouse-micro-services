package local.sop.datawarehouse.consent.saga.application.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import local.sop.datawarehouse.sharedlib.enums.ConsentPurpose;
import local.sop.datawarehouse.sharedlib.enums.ConsentStatus;
import local.sop.datawarehouse.sharedlib.enums.ConsentType;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.consent.saga.application.api.dto.ConsentResponse;
import local.sop.datawarehouse.consent.saga.application.api.dto.ConsentStatementResponse;
import local.sop.datawarehouse.consent.saga.application.infrastructure.consent.ConsentHttpAdapter;
import local.sop.datawarehouse.consent.saga.application.infrastructure.request.PayloadConsentCreate;
import local.sop.datawarehouse.consent.saga.application.infrastructure.request.PayloadGrantConsent;
import local.sop.datawarehouse.consent.saga.application.infrastructure.request.PayloadRevokeConsent;

@ExtendWith(MockitoExtension.class)
public class ConsentHttpAdapterTest {

    /* ----------------------------------------------------------- *
     *  ConsentHttpAdapter tests
     * ----------------------------------------------------------- */

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
    void consent_create_shouldReturnConsentStatementIdFromDownstream() {
        UUID expectedId = UUID.randomUUID();
        var request = new PayloadConsentCreate(true, "some text", ConsentPurpose.ANALYTICS, ConsentType.ONE_TIME);

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec    = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec       = mock(RestClient.ResponseSpec.class);

        when(consentClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/consents/statements")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadConsentCreate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ConsentStatementResponse.class))
                .thenReturn(new ConsentStatementResponse(expectedId, "some text", true, ConsentPurpose.ANALYTICS.name(), ConsentType.ONE_TIME.name()));

        UUID result = consentAdapter.create(request.active(), request.text(), request.purpose(), request.type());

        assertEquals(expectedId, result);
        verify(consentClient).post();
        verify(mockRequestBodyUriSpec).uri("/internal/consents/statements");
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(ConsentStatementResponse.class);
    }

    @Test
    void consent_get_shouldReturnConsentStatementFromDownstream() {
        UUID id = UUID.randomUUID();
        var expected = new ConsentStatementResponse(id, "some text", true, ConsentPurpose.ANALYTICS.name(), ConsentType.ONE_TIME.name());
        var mockRequestHeadersSpec    = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec          = mock(RestClient.ResponseSpec.class);

        doReturn(mockRequestHeadersUriSpec).when(consentClient).get();
        doReturn(mockRequestHeadersSpec).when(mockRequestHeadersUriSpec).uri("/internal/consents/statements/statement?id={id}", id);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ConsentStatementResponse.class)).thenReturn(expected);

        ConsentStatementResponse result = consentAdapter.getStatement(id);

        assertEquals(expected, result);
        verify(consentClient).get();
        verify(mockRequestHeadersUriSpec).uri("/internal/consents/statements/statement?id={id}", id);
        verify(mockResponseSpec).body(ConsentStatementResponse.class);
    }

    @Test
    void consent_compensate_shouldReturnResultFromDownstream() {
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        var request = new PayloadCompensateCreate(ConsentHttpAdapter.class, state);
        var expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec    = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec       = mock(RestClient.ResponseSpec.class);

        when(consentClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/consents/statements/{id}/compensate/create", id))
                .thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadCompensateCreate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

        ResponseCompensated result = consentAdapter.compensate(
                id, ConsentHttpAdapter.class, state);

        assertEquals(expectedResult.sagaState(), result.sagaState());
        assertEquals(expectedResult.success(), result.success());
        verify(consentClient).post();
        verify(mockRequestBodyUriSpec).uri("/internal/consents/statements/{id}/compensate/create", id);
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(ResponseCompensated.class);
    }

    @Test
    void consent_grant_shouldReturnConsentResponseFromDownstream() {
        UUID expectedId = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();
        UUID consentStatementRef = UUID.randomUUID();
        ConsentResponse expectedConsentResponse = new ConsentResponse(expectedId, personRef, "ACTIVE", consentStatementRef, "Statement Text", ConsentPurpose.ANALYTICS, ConsentType.ONE_TIME, true);
        
        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec    = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec       = mock(RestClient.ResponseSpec.class);

        when(consentClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/consents/consent/grant")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadGrantConsent.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ConsentResponse.class))
                .thenReturn(expectedConsentResponse);

        ConsentResponse result = consentAdapter.grant(personRef, consentStatementRef, ConsentStatus.ACTIVE);      

        assertEquals(expectedConsentResponse.active(), result.active());
        assertEquals(personRef, result.personReference());
        assertEquals(consentStatementRef, result.consentStatementId());
        assertEquals(ConsentPurpose.ANALYTICS, result.consentStatementPurpose());
        assertEquals(ConsentType.ONE_TIME, result.consentStatementType());
        assertEquals(ConsentStatus.ACTIVE.name(), result.status());

        verify(consentClient).post();
        verify(mockRequestBodyUriSpec).uri("/internal/consents/consent/grant");
        verify(mockRequestBodySpec).body(any(PayloadGrantConsent.class));
        verify(mockResponseSpec).body(ConsentResponse.class);
    }

    @Test
    void consent_withdraw_shouldReturnConsentResponseFromDownstream() {
        UUID expectedId = UUID.randomUUID();
        var request = new PayloadRevokeConsent(expectedId);

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec    = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec       = mock(RestClient.ResponseSpec.class);

        when(consentClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/consents/consent/withdraw")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadRevokeConsent.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ConsentResponse.class))
                .thenReturn(new ConsentResponse(expectedId, null, "WITHDRAWN", null, "some text", null, null, false));

        ConsentResponse result = consentAdapter.withdraw(request.consentId());

        assertEquals(expectedId, result.consentId());
        assertEquals(ConsentStatus.WITHDRAWN.name(), result.status());
        verify(consentClient).post();
        verify(mockRequestBodyUriSpec).uri("/internal/consents/consent/withdraw");
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(ConsentResponse.class);
    }

    @Test
    void consent_compensateConsent_shouldReturnResponseFromDownstream() {
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        Class<?> clazz = ConsentHttpAdapter.class;

        var expected = new ResponseCompensated(state, true);

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(consentClient.post()).thenReturn(mockRequestBodyUriSpec);

        when(mockRequestBodyUriSpec.uri(
                "/internal/consents/consent/{id}/compensate/create", id))
                .thenReturn(mockRequestBodySpec);

        when(mockRequestBodySpec.body(any(PayloadCompensateCreate.class)))
                .thenReturn(mockRequestBodySpec);

        when(mockRequestBodySpec.retrieve())
                .thenReturn(mockResponseSpec);

        when(mockResponseSpec.body(ResponseCompensated.class))
                .thenReturn(expected);

        ResponseCompensated result = consentAdapter.compensateConsent(id, clazz, state);

        assertEquals(expected.sagaState(), result.sagaState());
        assertEquals(expected.success(), result.success());

        verify(consentClient).post();

        verify(mockRequestBodyUriSpec).uri(
                "/internal/consents/consent/{id}/compensate/create",
                id);

        verify(mockRequestBodySpec).body(any(PayloadCompensateCreate.class));

        verify(mockResponseSpec).body(ResponseCompensated.class);
    }


    @SuppressWarnings("unchecked")
@Test
    void consent_getConsent_shouldReturnConsentResponseFromDownstream() {
        UUID id = UUID.randomUUID();

        ConsentResponse expected = new ConsentResponse(
                id,
                UUID.randomUUID(),
                "ACTIVE",
                UUID.randomUUID(),
                "statement text",
                ConsentPurpose.ANALYTICS,
                ConsentType.ONE_TIME,
                true);

        var mockRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        var mockRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(consentClient.get()).thenReturn(mockRequestHeadersUriSpec);

        when(mockRequestHeadersUriSpec.uri(
                "/internal/consents/consent/{id}", id))
                .thenReturn(mockRequestHeadersSpec);

        when(mockRequestHeadersSpec.retrieve())
                .thenReturn(mockResponseSpec);

        when(mockResponseSpec.body(ConsentResponse.class))
                .thenReturn(expected);

        ConsentResponse result = consentAdapter.getConsent(id);

        assertEquals(expected.consentId(), result.consentId());
        assertEquals(expected.status(), result.status());
        assertEquals(expected.consentStatementId(), result.consentStatementId());
        assertEquals(expected.consentStatementPurpose(), result.consentStatementPurpose());
        assertEquals(expected.consentStatementType(), result.consentStatementType());
        assertEquals(expected.active(), result.active());

        verify(consentClient).get();

        verify(mockRequestHeadersUriSpec).uri(
                "/internal/consents/consent/{id}",
                id);

        verify(mockResponseSpec).body(ConsentResponse.class);
    }

    @Test
    void consent_compensateConsentUpdate_shouldReturnResponseFromDownstream() {
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        Class<?> clazz = ConsentHttpAdapter.class;

        var expected = new ResponseCompensated(state, true);

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(consentClient.post()).thenReturn(mockRequestBodyUriSpec);

        when(mockRequestBodyUriSpec.uri(
                "/internal/consents/consent/{id}/compensate/update", id))
                .thenReturn(mockRequestBodySpec);

        when(mockRequestBodySpec.body(any(PayloadCompensateCreate.class)))
                .thenReturn(mockRequestBodySpec);

        when(mockRequestBodySpec.retrieve())
                .thenReturn(mockResponseSpec);

        when(mockResponseSpec.body(ResponseCompensated.class))
                .thenReturn(expected);

        ResponseCompensated result = consentAdapter.compensateConsentUpdate(id, clazz, state);

        assertEquals(expected.sagaState(), result.sagaState());
        assertEquals(expected.success(), result.success());

        verify(consentClient).post();

        verify(mockRequestBodyUriSpec).uri(
                "/internal/consents/consent/{id}/compensate/update",
                id);

        verify(mockRequestBodySpec).body(any(PayloadCompensateCreate.class));

        verify(mockResponseSpec).body(ResponseCompensated.class);
    }
}
