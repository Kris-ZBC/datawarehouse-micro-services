package local.sop.sopinfo.message.saga.application.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.message.saga.application.api.dto.InstructorResponse;
import local.sop.sopinfo.message.saga.application.infrastructure.instructor.InstructorHttpAdapter;

@ExtendWith(MockitoExtension.class)
public class InstructorHttpAdapterTest {

	@Mock(name = "instructor")
	private RestClient instructorClient;

	@Mock
	RestClient.RequestHeadersUriSpec<?> mockRequestHeadersUriSpec;

	private InstructorHttpAdapter instructorAdapter;

	@BeforeEach
	void setUp() {
		instructorAdapter = new InstructorHttpAdapter(instructorClient);
	}

	@Test
	void instructor_findById_shouldReturnInstructorResponseFromDownstream() {
		UUID id = UUID.randomUUID();
		var expected = new InstructorResponse(id, UUID.randomUUID());

		var mockRequestHeaderSpec = mock(RestClient.RequestHeadersSpec.class);
		var mockResponseSpec = mock(RestClient.ResponseSpec.class);

		doReturn(mockRequestHeadersUriSpec).when(instructorClient).get();
		doReturn(mockRequestHeaderSpec).when(mockRequestHeadersUriSpec).uri("/internal/instructors/{id}", id);
		when(mockRequestHeaderSpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(InstructorResponse.class)).thenReturn(expected);

		InstructorResponse result = instructorAdapter.findById(id);

		assertEquals(expected, result);
		verify(instructorClient).get();
		verify(mockRequestHeadersUriSpec).uri("/internal/instructors/{id}", id);
		verify(mockResponseSpec).body(InstructorResponse.class);
	}
}
