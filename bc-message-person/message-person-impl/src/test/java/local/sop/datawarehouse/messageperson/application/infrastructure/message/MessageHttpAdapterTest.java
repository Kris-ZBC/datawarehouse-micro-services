package local.sop.datawarehouse.messageperson.application.infrastructure.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import local.sop.datawarehouse.messageperson.application.infrastructure.response.MessageResponse;

@ExtendWith(MockitoExtension.class)
public class MessageHttpAdapterTest {

    @Mock(name = "message")
    private RestClient messageClient;

    @InjectMocks
    private MessageHttpAdapter adapter;

    @Test
    @SuppressWarnings("unchecked")
    public void findById_shouldReturnMessageResponse_whenExists() {
        UUID id = UUID.randomUUID();
        OffsetDateTime dateTimeSent = OffsetDateTime.now();
        UUID senderPersonRef = UUID.randomUUID();
        MessageResponse expectedResponse = new MessageResponse(id, dateTimeSent, "TestMessage", senderPersonRef);

        var mockRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        var mockRequestHeadersSpec    = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec          = mock(RestClient.ResponseSpec.class);
        var mockResponseEntity        = mock(ResponseEntity.class);

        when(messageClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri("/internal/messages/{id}", id))
                .thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.toEntity(MessageResponse.class))
                .thenReturn(mockResponseEntity);
        when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockResponseEntity.getBody()).thenReturn(expectedResponse);

        Optional<MessageResponse> result = adapter.findById(id);

        assertTrue(result.isPresent());
        assertEquals(expectedResponse, result.get());
        assertEquals(id, result.get().id());
        verify(messageClient).get();
        verify(mockRequestHeadersUriSpec).uri("/internal/messages/{id}", id);
        verify(mockResponseSpec).toEntity(MessageResponse.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void findById_shouldReturnEmptyOptional_whenNotFound() {
        UUID id = UUID.randomUUID();

        var mockRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        var mockRequestHeadersSpec    = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec          = mock(RestClient.ResponseSpec.class);
        var mockResponseEntity        = mock(ResponseEntity.class);

        when(messageClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri("/internal/messages/{id}", id))
                .thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.toEntity(MessageResponse.class))
                .thenReturn(mockResponseEntity);
        when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);

        Optional<MessageResponse> result = adapter.findById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByUd_shouldReturnEmpty_whenExceptionThrown() {
        UUID id = UUID.randomUUID();

        var mockRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        var mockRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(messageClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri("/internal/messages/{id}", id))
                .thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.toEntity(MessageResponse.class))
                .thenThrow(new RuntimeException("connection refused"));

        Optional<MessageResponse> result = adapter.findById(id);

        assertTrue(result.isEmpty());
        verify(messageClient).get();
    }

}
