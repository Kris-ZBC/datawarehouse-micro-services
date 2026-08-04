package local.sop.sopinfo.login.saga.application.infrastructure.auditlog;

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

import local.sop.sopinfo.login.saga.application.api.dto.AuditlogResponse;
import local.sop.sopinfo.login.saga.application.infrastructure.request.PayloadAuditLogCreate;
import local.sop.sopinfo.login.saga.application.infrastructure.request.PayloadCompensate;
import local.sop.sopinfo.login.saga.application.infrastructure.response.ResponseCompensated;
import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

@ExtendWith(MockitoExtension.class)
class AuditlogHttpAdapterTest {

    @Mock(name = "auditlog")
    private RestClient auditlogClient;

    private AuditlogHttpAdapter auditlogAdapter;

    @BeforeEach
    void setUp() {
        auditlogAdapter = new AuditlogHttpAdapter(auditlogClient);
    }

    @Test
    void create_shouldReturnAuditlogIdFromDownstream() {
        UUID expectedId = UUID.randomUUID();
        PayloadAuditLogCreate request = new PayloadAuditLogCreate(
                UUID.randomUUID(),
                ActorType.USER,
                Severity.INFO,
                "originSystem",
                "originService",
                "originComponent",
                "data",
                "description");

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(auditlogClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/internal/auditlogs/create")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(PayloadAuditLogCreate.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(AuditlogResponse.class)).thenReturn(new AuditlogResponse(
                expectedId,
                request.actor(),
                request.type(),
                request.severity(),
                request.originSystem(),
                request.originService(),
                request.originComponent(),
                request.data(),
                request.description(),
                null));

        UUID result = auditlogAdapter.create(
                request.actor(),
                request.type(),
                request.severity(),
                request.originSystem(),
                request.originService(),
                request.originComponent(),
                request.data(),
                request.description());

        assertEquals(expectedId, result);
        verify(auditlogClient).post();
        verify(requestBodyUriSpec).uri("/internal/auditlogs/create");
        verify(requestBodySpec).body(eq(request));
        verify(responseSpec).body(AuditlogResponse.class);
    }

    @Test
    void compensate_shouldReturnCompensationResultFromDownstream() {
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        PayloadCompensate request = new PayloadCompensate(id, AuditlogHttpAdapter.class, state);
        ResponseCompensated expected = new ResponseCompensated(state, true);

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(auditlogClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/internal/auditlogs/compensate")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(PayloadCompensate.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ResponseCompensated.class)).thenReturn(expected);

        ResponseCompensated result = auditlogAdapter.compensate(id, AuditlogHttpAdapter.class, state);

        assertEquals(expected.sagaState(), result.sagaState());
        assertEquals(expected.success(), result.success());
        verify(auditlogClient).post();
        verify(requestBodyUriSpec).uri("/internal/auditlogs/compensate");
        verify(requestBodySpec).body(eq(request));
        verify(responseSpec).body(ResponseCompensated.class);
    }
}
