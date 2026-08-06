package local.sop.datawarehouse.message.saga.application.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import local.sop.datawarehouse.message.saga.application.api.dto.PersonNotificationResponse;
import local.sop.datawarehouse.message.saga.application.infrastructure.personnotification.PersonNotificationHttpAdapter;
import local.sop.datawarehouse.message.saga.application.infrastructure.request.PayloadPersonNotificationCreate;

@ExtendWith(MockitoExtension.class)
public class PersonNotificationHttpAdapterTest {

	@Mock(name = "personnotification")
	private RestClient personNotificationClient;

	@Mock
	RestClient.RequestHeadersUriSpec<?> mockRequestHeadersUriSpec;

	private PersonNotificationHttpAdapter personNotificationAdapter;

	@BeforeEach
	void setUp() {
		personNotificationAdapter = new PersonNotificationHttpAdapter(personNotificationClient);
	}

	@Test
	void personNotification_create_shouldCallPostOnDownstream() {
		UUID personRef = UUID.randomUUID();
		UUID notificationRef = UUID.randomUUID();

		var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
		var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
		var mockResponseSpec = mock(RestClient.ResponseSpec.class);

		when(personNotificationClient.post()).thenReturn(mockRequestBodyUriSpec);
		when(mockRequestBodyUriSpec.uri("/internal/person-notifications")).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.body(any(PayloadPersonNotificationCreate.class))).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);

		personNotificationAdapter.create(personRef, notificationRef);

		verify(personNotificationClient).post();
		verify(mockRequestBodyUriSpec).uri("/internal/person-notifications");
		verify(mockRequestBodySpec).body(any(PayloadPersonNotificationCreate.class));
		verify(mockResponseSpec).toBodilessEntity();
	}

	@Test
	void personNotification_findByNotificationRef_shouldReturnListFromDownstream() {
		UUID id = UUID.randomUUID();
		var expected = List.of(new PersonNotificationResponse(UUID.randomUUID(), UUID.randomUUID(), id));

		var mockRequestHeaderSpec = mock(RestClient.RequestHeadersSpec.class);
		var mockResponseSpec = mock(RestClient.ResponseSpec.class);

		doReturn(mockRequestHeadersUriSpec).when(personNotificationClient).get();
		doReturn(mockRequestHeaderSpec).when(mockRequestHeadersUriSpec).uri("/internal/person-notifications?notificationRef={notificationRef}", id);
		when(mockRequestHeaderSpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(new ParameterizedTypeReference<List<PersonNotificationResponse>>() {})).thenReturn(expected);

		List<PersonNotificationResponse> result = personNotificationAdapter.findByNotificationRef(id);

		assertEquals(expected, result);
    	verify(personNotificationClient).get();
    	verify(mockRequestHeadersUriSpec).uri("/internal/person-notifications?notificationRef={notificationRef}", id);
    	verify(mockResponseSpec).body(new ParameterizedTypeReference<List<PersonNotificationResponse>>() {});
	}
}
