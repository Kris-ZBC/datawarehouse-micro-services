package local.sop.datawarehouse.message.saga.application.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.message.saga.application.api.dto.MessageResponse;
import local.sop.datawarehouse.message.saga.application.infrastructure.message.MessageHttpAdapter;
import local.sop.datawarehouse.message.saga.application.infrastructure.request.PayloadMessageCreate;

@ExtendWith(MockitoExtension.class)
public class MessageHttpAdapterTest {
	
	@Mock(name = "message")
	private RestClient messageClient;

	@Mock
    RestClient.RequestHeadersUriSpec<?> mockRequestHeadersUriSpec;

	private MessageHttpAdapter messageAdapter;

	@BeforeEach
	void setUp() {
		messageAdapter = new MessageHttpAdapter(messageClient);
	}

	@Test
	void message_create_shouldReturnMessageResponseFromDownstream() {
        UUID senderPersonRef = UUID.randomUUID();
        var request = new PayloadMessageCreate(senderPersonRef, "test message");
        var expected = new MessageResponse(UUID.randomUUID(), OffsetDateTime.now(), "test message", senderPersonRef);

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(messageClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/messages")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadMessageCreate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(MessageResponse.class)).thenReturn(expected);

        MessageResponse result = messageAdapter.create(senderPersonRef, "test message");

        assertEquals(expected, result);
        verify(messageClient).post();
        verify(mockRequestBodyUriSpec).uri("/internal/messages");
        verify(mockRequestBodySpec).body(request);
        verify(mockResponseSpec).body(MessageResponse.class);
	}

	@Test
	void message_findById_shouldReturnMessageResponseFromDownstream() {
		UUID id = UUID.randomUUID();
		var expected = new MessageResponse(id, OffsetDateTime.now(), "test message", UUID.randomUUID());

		var mockRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
		var mockResponseSpec = mock(RestClient.ResponseSpec.class);

		doReturn(mockRequestHeadersUriSpec).when(messageClient).get();
		doReturn(mockRequestHeadersSpec).when(mockRequestHeadersUriSpec).uri("/internal/messages/{id}", id);
		when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(MessageResponse.class)).thenReturn(expected);

		MessageResponse result = messageAdapter.findById(id);
		
		assertEquals(expected, result);
		verify(messageClient).get();
		verify(mockRequestHeadersUriSpec).uri("/internal/messages/{id}", id);
		verify(mockResponseSpec).body(MessageResponse.class);
	}

	@Test
	void message_compensate_shouldReturnResultFromDownstream() {
		UUID id = UUID.randomUUID();
		SagaOutcome state = SagaOutcome.COMPENSATED;
		var request = new PayloadCompensateCreate(MessageHttpAdapter.class, state);
		var expected = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(messageClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/messages/{id}/compensate/create", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadCompensateCreate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expected);

        ResponseCompensated result = messageAdapter.compensate(id, MessageHttpAdapter.class, state);

        assertEquals(expected.sagaState(), result.sagaState());
        assertEquals(expected.success(), result.success());
        verify(messageClient).put();
        verify(mockRequestBodyUriSpec).uri("/internal/messages/{id}/compensate/create", id);
        verify(mockRequestBodySpec).body(request);
        verify(mockResponseSpec).body(ResponseCompensated.class);
	}
}
