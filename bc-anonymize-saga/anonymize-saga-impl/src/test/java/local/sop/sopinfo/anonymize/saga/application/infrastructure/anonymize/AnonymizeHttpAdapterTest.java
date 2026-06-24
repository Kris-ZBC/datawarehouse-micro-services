package local.sop.sopinfo.anonymize.saga.application.infrastructure.anonymize;

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.anonymize.saga.application.api.dto.AnonymizeResponse;
import local.sop.sopinfo.anonymize.saga.application.infrastructure.request.PayloadAnonymizeCreate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnonymizeHttpAdapterTest {

    @Mock
    private RestClient restClient;
    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private RestClient.RequestBodySpec requestBodySpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    private AnonymizeHttpAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AnonymizeHttpAdapter(restClient);
    }

    @Test
    void create_ShouldSendCorrectPayloadAndReturnId() {
        UUID personRef = UUID.randomUUID();
        UUID anonymizationId = UUID.randomUUID();

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/internal/anonymizations")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(PayloadAnonymizeCreate.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(AnonymizeResponse.class)).thenReturn(new AnonymizeResponse(anonymizationId, personRef));

        UUID result = adapter.create(personRef);

        assertEquals(anonymizationId, result);
        verify(requestBodySpec).body(argThat((PayloadAnonymizeCreate payload) -> payload.personRef().equals(personRef)));
    }

    @Test
    void create_ShouldThrowRuntimeException() {
        UUID personRef = UUID.randomUUID();

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/internal/anonymizations")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(PayloadAnonymizeCreate.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RuntimeException("downstream failed"));

        assertThrows(RuntimeException.class, () -> adapter.create(personRef));
    }

    @Test
    void compensate_ShouldSendCorrectPayloadAndReturnResponse() {
        UUID id = UUID.randomUUID();
        ResponseCompensated expected = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(eq("/internal/anonymizations/{id}/compensate/create"), eq(id))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(PayloadCompensateCreate.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ResponseCompensated.class)).thenReturn(expected);

        ResponseCompensated result = adapter.compensate(id, AnonymizeHttpAdapterTest.class, SagaOutcome.COMPENSATED);

        assertSame(expected, result);
        verify(requestBodySpec).body(argThat((PayloadCompensateCreate payload) ->
                payload.clazz().equals(AnonymizeHttpAdapterTest.class) && payload.sagaState() == SagaOutcome.COMPENSATED));
    }

    @Test
    void compensate_ShouldThrowRuntimeException() {
        UUID id = UUID.randomUUID();

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(eq("/internal/anonymizations/{id}/compensate/create"), eq(id))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(PayloadCompensateCreate.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RuntimeException("downstream failed"));

        assertThrows(RuntimeException.class, () -> adapter.compensate(id, AnonymizeHttpAdapterTest.class, SagaOutcome.COMPENSATED));
    }
}
