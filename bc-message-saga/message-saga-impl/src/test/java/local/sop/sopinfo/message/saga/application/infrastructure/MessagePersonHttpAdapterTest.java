package local.sop.sopinfo.message.saga.application.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.message.saga.application.api.dto.MessagePersonResponse;
import local.sop.sopinfo.message.saga.application.infrastructure.messageperson.MessagePersonHttpAdapter;
import local.sop.sopinfo.message.saga.application.infrastructure.request.PayloadMessagePersonCreate;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;

@ExtendWith(MockitoExtension.class)
public class MessagePersonHttpAdapterTest {

	@Mock(name = "messageperson")
	private RestClient messagePersonClient;

	@Mock
	RestClient.RequestHeadersUriSpec<?> mockRequestHeadersUriSpec;

	private MessagePersonHttpAdapter messagePersonAdapter;

	@BeforeEach
	void setUp() {
		messagePersonAdapter = new MessagePersonHttpAdapter(messagePersonClient);
	}

	@Test
	void messagePerson_create_shouldReturnMessagePersonIdFromDownstream() {
		UUID personRef = UUID.randomUUID();
    	UUID messageRef = UUID.randomUUID();

    	var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
    	var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
    	var mockResponseSpec = mock(RestClient.ResponseSpec.class);

    	when(messagePersonClient.post()).thenReturn(mockRequestBodyUriSpec);
    	when(mockRequestBodyUriSpec.uri("/internal/message-persons")).thenReturn(mockRequestBodySpec);
    	when(mockRequestBodySpec.body(any(PayloadMessagePersonCreate.class))).thenReturn(mockRequestBodySpec);
    	when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
    	when(mockResponseSpec.body(MessagePersonResponse.class)).thenReturn(new MessagePersonResponse(new CompositeKey(personRef, messageRef), true, LocalDateTime.now()));

		CompositeKey result = messagePersonAdapter.create(personRef, messageRef);

		assertEquals(personRef, result.key(0));
		assertEquals(messageRef, result.key(1));
		verify(messagePersonClient).post();
		verify(mockRequestBodyUriSpec).uri("/internal/message-persons");
		verify(mockRequestBodySpec).body(any(PayloadMessagePersonCreate.class));
		verify(mockResponseSpec).body(MessagePersonResponse.class);
	}

	@Test
	void messagePerson_findByMessageRef_shouldReturnListFromDownstream() {
		UUID id = UUID.randomUUID();
		var expected = List.of(new MessagePersonResponse(new CompositeKey(UUID.randomUUID(), UUID.randomUUID()), true, LocalDateTime.now()));

		var mockRequestHeaderSpec = mock(RestClient.RequestHeadersSpec.class);
		var mockResponseSpec = mock(RestClient.ResponseSpec.class);

		doReturn(mockRequestHeadersUriSpec).when(messagePersonClient).get();
		doReturn(mockRequestHeaderSpec).when(mockRequestHeadersUriSpec).uri("/internal/message-persons/by-message/{messageId}", id);
		when(mockRequestHeaderSpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(new ParameterizedTypeReference<List<MessagePersonResponse>>() {})).thenReturn(expected);

		List<MessagePersonResponse> result = messagePersonAdapter.findByMessageRef(id);

		assertEquals(expected, result);
    	verify(messagePersonClient).get();
    	verify(mockRequestHeadersUriSpec).uri("/internal/message-persons/by-message/{messageId}", id);
    	verify(mockResponseSpec).body(new ParameterizedTypeReference<List<MessagePersonResponse>>() {});
	}
}
