package local.sop.datawarehouse.message.saga.application.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import local.sop.datawarehouse.message.saga.application.api.dto.NotificationResponse;
import local.sop.datawarehouse.message.saga.application.infrastructure.notification.NotificationHttpAdapter;
import local.sop.datawarehouse.message.saga.application.infrastructure.request.PayloadNotificationCreate;

@ExtendWith(MockitoExtension.class)
public class NotificationHttpAdapterTest {

	@Mock(name = "notification")
	private RestClient notificationClient;

	@Mock
	RestClient.RequestHeadersUriSpec<?> mockRequestHeadersUriSpec;

	private NotificationHttpAdapter notificationAdapter;

	@BeforeEach
	void setUp() {
		notificationAdapter = new NotificationHttpAdapter(notificationClient);
	}

	@Test
	void notification_create_shouldReturnNotificationResponseFromDownstream() {
		UUID messageRef = UUID.randomUUID();
		UUID expectedId = UUID.randomUUID();
		var request = new PayloadNotificationCreate(messageRef);

		var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
		var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
		var mockResponseSpec = mock(RestClient.ResponseSpec.class);

		when(notificationClient.post()).thenReturn(mockRequestBodyUriSpec);
		when(mockRequestBodyUriSpec.uri("/internal/notifications/create")).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.body(any(PayloadNotificationCreate.class))).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(NotificationResponse.class)).thenReturn(new NotificationResponse(expectedId, false, LocalDateTime.now(), messageRef));

		UUID result = notificationAdapter.create(messageRef);

		assertEquals(expectedId, result);
		verify(notificationClient).post();
		verify(mockRequestBodyUriSpec).uri("/internal/notifications/create");
		verify(mockRequestBodySpec).body(eq(request));
		verify(mockResponseSpec).body(NotificationResponse.class);
	}

	@Test
	void notification_findById_shouldReturnNotificationResponseFromDownstream() {
		UUID id = UUID.randomUUID();
		var expected = new NotificationResponse(id, false, LocalDateTime.now(), UUID.randomUUID());

		var mockRequestHeaderSpec = mock(RestClient.RequestHeadersUriSpec.class);
		var mockResponseSpec = mock(RestClient.ResponseSpec.class);

		doReturn(mockRequestHeaderSpec).when(notificationClient).get();
		doReturn(mockRequestHeaderSpec).when(mockRequestHeaderSpec).uri("/internal/notifications/{id}", id);
		when(mockRequestHeaderSpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(NotificationResponse.class)).thenReturn(expected);

		NotificationResponse result = notificationAdapter.findById(id);

		assertEquals(expected, result);
		verify(notificationClient).get();
		verify(mockRequestHeaderSpec).uri("/internal/notifications/{id}", id);
		verify(mockResponseSpec).body(NotificationResponse.class);
	}

	@Test
	void notification_deleteNotification_shouldCallDeleteOnDownstream() {
    	UUID id = UUID.randomUUID();

    	var mockRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
    	var mockResponseSpec = mock(RestClient.ResponseSpec.class);

    	doReturn(mockRequestHeadersUriSpec).when(notificationClient).delete();
    	doReturn(mockRequestHeadersSpec).when(mockRequestHeadersUriSpec).uri("/internal/notifications/{id}", id);
    	when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);

    	notificationAdapter.deleteNotification(id);

    	verify(notificationClient).delete();
    	verify(mockRequestHeadersUriSpec).uri("/internal/notifications/{id}", id);
    	verify(mockResponseSpec).toBodilessEntity();
	}

		@Test
	void notification_compensate_shouldReturnResultFromDownstream() {
		UUID id = UUID.randomUUID();
		SagaOutcome state = SagaOutcome.COMPENSATED;
		var request = new PayloadCompensateCreate(NotificationHttpAdapter.class, state);
		var expected = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(notificationClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/notifications/{id}/compensate/create", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadCompensateCreate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expected);

        ResponseCompensated result = notificationAdapter.compensate(id, NotificationHttpAdapter.class, state);

        assertEquals(expected.sagaState(), result.sagaState());
        assertEquals(expected.success(), result.success());
        verify(notificationClient).put();
        verify(mockRequestBodyUriSpec).uri("/internal/notifications/{id}/compensate/create", id);
        verify(mockRequestBodySpec).body(request);
        verify(mockResponseSpec).body(ResponseCompensated.class);
	}
}
