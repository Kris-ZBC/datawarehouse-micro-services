package local.sop.sopinfo.message.saga.application.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.message.saga.application.api.dto.EducationInstructorResponse;
import local.sop.sopinfo.message.saga.application.infrastructure.educationinstructor.EducationInstructorHttpAdapter;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

@ExtendWith(MockitoExtension.class)
public class EducationInstructorHttpAdapterTest {

	@Mock(name = "educationinstructor")
	private RestClient educationInstructorClient;

	@Mock
	RestClient.RequestHeadersUriSpec<?> mockRequestHeadersUriSpec;

	private EducationInstructorHttpAdapter educationInstructorAdapter;

	@BeforeEach
	void setUp() {
		educationInstructorAdapter = new EducationInstructorHttpAdapter(educationInstructorClient);
	}

	@Test
	void educationInstructor_findByInstructorRef_shouldReturnListFromDownstream() {
		UUID id = UUID.randomUUID();
		var expected = List.of(new EducationInstructorResponse( new CompositeKey(UUID.randomUUID(), UUID.randomUUID()), Instant.now(), true));

		var mockRequestHeaderSpec = mock(RestClient.RequestHeadersSpec.class);
		var mockResponseSpec = mock(RestClient.ResponseSpec.class);

		doReturn(mockRequestHeadersUriSpec).when(educationInstructorClient).get();
		doReturn(mockRequestHeaderSpec).when(mockRequestHeadersUriSpec).uri("/internal/educationinstructors/by-education-ref/{id}", id);
		when(mockRequestHeaderSpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(new ParameterizedTypeReference<List<EducationInstructorResponse>>() {})).thenReturn(expected);

		List<EducationInstructorResponse> result = educationInstructorAdapter.findByEducationRef(id);

		assertEquals(expected, result);
    	verify(educationInstructorClient).get();
    	verify(mockRequestHeadersUriSpec).uri("/internal/educationinstructors/by-education-ref/{id}", id);
    	verify(mockResponseSpec).body(new ParameterizedTypeReference<List<EducationInstructorResponse>>() {});
	}
}
