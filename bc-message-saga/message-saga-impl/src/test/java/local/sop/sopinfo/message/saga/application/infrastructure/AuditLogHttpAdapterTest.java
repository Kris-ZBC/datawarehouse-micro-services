package local.sop.sopinfo.message.saga.application.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.message.saga.application.api.dto.AuditLogResponse;
import local.sop.sopinfo.message.saga.application.infrastructure.auditlog.AuditLogHttpAdapter;
import local.sop.sopinfo.message.saga.application.api.dto.CreateAuditLogCmd;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;

@ExtendWith(MockitoExtension.class)
class AuditLogHttpAdapterTest {
    /*
     * ----------------------------------------------------------- *
     * AuditLogHttpAdapterTest tests
     * -----------------------------------------------------------
     */

    @Mock(name = "auditlog")
    private RestClient auditlogClient;

    @Mock
    RestClient.RequestHeadersUriSpec<?> mockRequestHeadersUriSpec;

    private AuditLogHttpAdapter auditLogAdapter;

    @BeforeEach
    void setUp() {
        auditLogAdapter = new AuditLogHttpAdapter(auditlogClient);
    }

    @Test
    void auditlog_get_shouldReturnAuditlogFromDownstream() {
        UUID id = UUID.randomUUID();
        var expected = new AuditLogResponse(id, UUID.randomUUID(), null, null, "originSystem", "originService",
                "originComponent", "data", "description", null);
        var mockRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        doReturn(mockRequestHeadersUriSpec).when(auditlogClient).get();
        doReturn(mockRequestHeadersSpec).when(mockRequestHeadersUriSpec)
                .uri("/internal/auditlogs/{id}", id);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(AuditLogResponse.class)).thenReturn(expected);

        AuditLogResponse result = auditLogAdapter.getById(id);

        assertEquals(expected, result);
        verify(auditlogClient).get();
        verify(mockRequestHeadersUriSpec).uri("/internal/auditlogs/{id}", id);
        verify(mockResponseSpec).body(AuditLogResponse.class);
    }

    @Test
    void auditlog_compensate_shouldReturnResultFromDownstream() {
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        var request = new PayloadCompensateCreate(AuditLogHttpAdapter.class, state);
        var expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(auditlogClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/auditlogs/{id}/compensate/create", id))
                .thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadCompensateCreate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

        ResponseCompensated result = auditLogAdapter.compensate(id, AuditLogHttpAdapter.class, state);

        assertEquals(expectedResult.sagaState(), result.sagaState());
        assertEquals(expectedResult.success(), result.success());
        verify(auditlogClient).put();
        verify(mockRequestBodyUriSpec).uri("/internal/auditlogs/{id}/compensate/create", id);
        verify(mockRequestBodySpec).body(request);
        verify(mockResponseSpec).body(ResponseCompensated.class);
    }
    @Test
    void auditlog_create_shouldReturnUuidFromDownstream() {
        UUID expectedId = UUID.randomUUID();
        var request = new CreateAuditLogCmd(expectedId, null, null, "originSystem", "originService", "originComponent", "data", "description");

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(auditlogClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/auditlogs")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(CreateAuditLogCmd.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(UUID.class)).thenReturn(expectedId);

        UUID result = auditLogAdapter.create(request);
        assertEquals(expectedId, result);
        verify(auditlogClient).post();
        verify(mockRequestBodyUriSpec).uri("/internal/auditlogs");
        verify(mockRequestBodySpec).body(request);
        verify(mockResponseSpec).body(UUID.class);
    }
}
