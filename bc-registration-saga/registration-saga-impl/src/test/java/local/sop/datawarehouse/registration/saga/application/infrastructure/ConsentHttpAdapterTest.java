package local.sop.datawarehouse.registration.saga.application.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import local.sop.datawarehouse.sharedlib.enums.ConsentType;
import local.sop.datawarehouse.registration.saga.application.api.dto.consent.ConsentResponse;
import local.sop.datawarehouse.registration.saga.application.infrastructure.consent.ConsentHttpAdapter;

/**
 * Tests ConsentHttpAdapter — the direct-to-bc-consent READ path.
 * getById() only: getConsentStatementById() was removed entirely
 * (registration-saga has no legitimate reason to know anything about a
 * ConsentStatement — that's consent's own concern), so there's nothing
 * left to test on that side. ConsentSagaHttpAdapterTest covers the
 * consent-saga-routed WRITE path (grant / compensateConsent).
 */
@ExtendWith(MockitoExtension.class)
class ConsentHttpAdapterTest {

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
        var expected = new ConsentResponse(id, UUID.randomUUID(), "ACTIVE", UUID.randomUUID(), "consentStatement", ConsentPurpose.RESEARCH, ConsentType.ONE_TIME, true);
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
}