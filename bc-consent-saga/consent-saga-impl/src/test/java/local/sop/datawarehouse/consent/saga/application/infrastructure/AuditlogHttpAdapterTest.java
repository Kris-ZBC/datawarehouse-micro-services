package local.sop.datawarehouse.consent.saga.application.infrastructure;

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

import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.consent.saga.application.api.dto.ResponseAuditlog;
import local.sop.datawarehouse.consent.saga.application.infrastructure.auditlog.AuditlogHttpAdapter;
import local.sop.datawarehouse.consent.saga.application.infrastructure.request.PayloadAuditlogCreate;

@ExtendWith(MockitoExtension.class)
public class AuditlogHttpAdapterTest {
    /* ----------------------------------------------------------- *
     *  AuditlogHttpAdapter tests
     * ----------------------------------------------------------- */

    @Mock(name = "auditlog")
    private RestClient auditlogClient;

    
    private AuditlogHttpAdapter auditlogAdapter;

    @BeforeEach
    void setUp() {
        auditlogAdapter = new AuditlogHttpAdapter(auditlogClient);
    }


    @Test
    void auditlog_create_shouldReturnUuidFromDownstream() {
        UUID expectedId = UUID.randomUUID();
        var request = new PayloadAuditlogCreate(
                UUID.randomUUID(), ActorType.USER, Severity.INFO,
                "originSystem", "originService", "originComponent",
                "data", "description");

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec    = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec       = mock(RestClient.ResponseSpec.class);



        when(auditlogClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/auditlogs")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadAuditlogCreate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseAuditlog.class)).thenReturn(new ResponseAuditlog(expectedId));

        UUID result = auditlogAdapter.create(
                request.actor(), request.type(), request.severity(),
                request.originSystem(), request.originService(), request.originComponent(),
                request.data(), request.description());

        assertEquals(expectedId, result);
        verify(auditlogClient).post();
        verify(mockRequestBodyUriSpec).uri("/internal/auditlogs");
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(ResponseAuditlog.class);
    }

    @Test
    void auditlog_compensate_shouldReturnResultFromDownstream() {
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        var request = new PayloadCompensateCreate(AuditlogHttpAdapter.class, state);
        var expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec    = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec       = mock(RestClient.ResponseSpec.class);

        when(auditlogClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/auditlogs/{id}/compensate/create", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadCompensateCreate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

        ResponseCompensated result = auditlogAdapter.compensate(id, AuditlogHttpAdapter.class, state);

        assertEquals(expectedResult.sagaState(), result.sagaState());
        assertEquals(expectedResult.success(), result.success());
        verify(auditlogClient).put();
        verify(mockRequestBodyUriSpec).uri("/internal/auditlogs/{id}/compensate/create", id);
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(ResponseCompensated.class);
    }

}
