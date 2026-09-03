package local.sop.datawarehouse.anonymize.saga.application.infrastructure.auditlog;

import java.util.UUID;

import local.sop.datawarehouse.sharedlib.enums.ActorType;
import local.sop.datawarehouse.sharedlib.enums.Severity;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.anonymize.saga.application.api.dto.ResponseAuditlog;
import local.sop.datawarehouse.anonymize.saga.application.infrastructure.request.PayloadAuditlogCreate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditlogHttpAdapterTest {

    @Mock
    private RestClient restClient;
    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private RestClient.RequestBodySpec requestBodySpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    private AuditlogHttpAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AuditlogHttpAdapter(restClient);
    }

    @Test
    void create_ShouldSendCorrectPayloadAndReturnId() {
        UUID actor = UUID.randomUUID();
        UUID expectedId = UUID.randomUUID();

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/internal/auditlogs/create")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(PayloadAuditlogCreate.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ResponseAuditlog.class)).thenReturn(new ResponseAuditlog(expectedId));

        UUID result = adapter.create(
                actor,
                ActorType.USER,
                Severity.INFO,
                "originSystem",
                "originService",
                "originComponent",
                "data",
                "description"
        );

        assertEquals(expectedId, result);
        verify(requestBodySpec).body(argThat((PayloadAuditlogCreate payload) ->
                payload.actor().equals(actor)
                        && payload.type() == ActorType.USER
                        && payload.severity() == Severity.INFO
                        && payload.originSystem().equals("originSystem")
                        && payload.originService().equals("originService")
                        && payload.originComponent().equals("originComponent")
                        && payload.data().equals("data")
                        && payload.description().equals("description")));
    }

    @Test
    void create_ShouldThrowRuntimeException() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/internal/auditlogs/create")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(PayloadAuditlogCreate.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RuntimeException("downstream failed"));

        assertThrows(RuntimeException.class, () -> adapter.create(
                UUID.randomUUID(),
                ActorType.USER,
                Severity.INFO,
                "originSystem",
                "originService",
                "originComponent",
                "data",
                "description"
        ));
    }

    @Test
    void compensate_ShouldSendCorrectPayloadAndReturnResponse() {
        UUID id = UUID.randomUUID();
        ResponseCompensated expected = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(eq("/internal/auditlogs/{id}/compensate/create"), eq(id))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(PayloadCompensateCreate.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ResponseCompensated.class)).thenReturn(expected);

        ResponseCompensated result = adapter.compensate(id, AuditlogHttpAdapterTest.class, SagaOutcome.COMPENSATED);

        assertSame(expected, result);
        verify(requestBodySpec).body(argThat((PayloadCompensateCreate payload) ->
                payload.clazz().equals(AuditlogHttpAdapterTest.class)
                        && payload.sagaState() == SagaOutcome.COMPENSATED));
    }

    @Test
    void compensate_ShouldThrowRuntimeException() {
        UUID id = UUID.randomUUID();

        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(eq("/internal/auditlogs/{id}/compensate/create"), eq(id))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(PayloadCompensateCreate.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RuntimeException("downstream failed"));

        assertThrows(RuntimeException.class,
                () -> adapter.compensate(id, AuditlogHttpAdapterTest.class, SagaOutcome.COMPENSATED));
    }
}
