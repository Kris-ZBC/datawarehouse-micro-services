package local.sop.sopinfo.message.saga.application.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
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

import local.sop.sopinfo.message.saga.application.api.dto.PersonResponse;
import local.sop.sopinfo.message.saga.application.infrastructure.person.PersonHttpAdapter;

@ExtendWith(MockitoExtension.class)
public class PersonHttpAdapterTest {

	@Mock(name = "person")
	private RestClient personClient;

	@Mock
	RestClient.RequestHeadersUriSpec<?> mockRequestHeadersUriSpec;

	private PersonHttpAdapter personAdapter;

	@BeforeEach
	void setUp() {
		personAdapter = new PersonHttpAdapter(personClient);
	}

	@Test
	void person_findById_shouldReturnPersonResponseFromDownstream() {
		UUID id = UUID.randomUUID();
		var expected = new PersonResponse(id, "John", "Doe", "Johndoe@zbc.dk", UUID.randomUUID());

		var mockRequestHeaderSpec = mock(RestClient.RequestHeadersSpec.class);
		var mockResponseSpec = mock(RestClient.ResponseSpec.class);

		doReturn(mockRequestHeadersUriSpec).when(personClient).get();
		doReturn(mockRequestHeaderSpec).when(mockRequestHeadersUriSpec).uri("/internal/persons/{id}", id);
		when(mockRequestHeaderSpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(PersonResponse.class)).thenReturn(expected);

		PersonResponse result = personAdapter.findById(id);

		assertEquals(expected, result);
		verify(personClient).get();
		verify(mockRequestHeadersUriSpec).uri("/internal/persons/{id}", id);
		verify(mockResponseSpec).body(PersonResponse.class);
	}
}
