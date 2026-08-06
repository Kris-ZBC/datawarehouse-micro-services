package local.sop.datawarehouse.personnotification.application.infrastructure.notification;

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

import local.sop.datawarehouse.personnotification.application.infrastructure.response.NotificationResponse;

@ExtendWith(MockitoExtension.class)
public class NotificationHttpAdapterTest {

    @Mock(name = "notification")

    private RestClient notificationClient;

    @InjectMocks
    private NotificationHttpAdapter adapter;

    @Test
    @SuppressWarnings("unchecked")
    public void findById_shouldReturnNotificationResponse_whenExists() {
        UUID id = UUID.randomUUID();
        UUID notificationRef = UUID.randomUUID();
        OffsetDateTime dateTimeSent = OffsetDateTime.now();
        boolean seen = false;
        NotificationResponse expectedResponse = new NotificationResponse(id, notificationRef, dateTimeSent.toLocalDateTime(), seen);

        var mockRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        var mockRequestHeadersSpec    = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec          = mock(RestClient.ResponseSpec.class);
        var mockResponseEntity        = mock(ResponseEntity.class);

        when(notificationClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri("/internal/notifications/{id}", id))
                .thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.toEntity(NotificationResponse.class))
                .thenReturn(mockResponseEntity);
        when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockResponseEntity.getBody()).thenReturn(expectedResponse);

        Optional<NotificationResponse> result = adapter.findById(id);

        assertTrue(result.isPresent());
        assertEquals(expectedResponse, result.get());
        assertEquals(id, result.get().id());
        verify(notificationClient).get();
        verify(mockRequestHeadersUriSpec).uri("/internal/notifications/{id}", id);
        verify(mockResponseSpec).toEntity(NotificationResponse.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void findById_shouldReturnEmptyOptional_whenNotFound() {
        UUID id = UUID.randomUUID();

        var mockRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        var mockRequestHeadersSpec    = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec          = mock(RestClient.ResponseSpec.class);
        var mockResponseEntity        = mock(ResponseEntity.class);

        when(notificationClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri("/internal/notifications/{id}", id))
                .thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.toEntity(NotificationResponse.class))
                .thenReturn(mockResponseEntity);
        when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);

        Optional<NotificationResponse> result = adapter.findById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByUd_shouldReturnEmpty_whenExceptionThrown() {
        UUID id = UUID.randomUUID();

        var mockRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        var mockRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(notificationClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri("/internal/notifications/{id}", id))
                .thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.toEntity(NotificationResponse.class))
                .thenThrow(new RuntimeException("connection refused"));

        Optional<NotificationResponse> result = adapter.findById(id);

        assertTrue(result.isEmpty());
        verify(notificationClient).get();
    }

}
