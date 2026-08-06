package local.sop.datawarehouse.message.saga.application.infrastructure;

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

import local.sop.datawarehouse.message.saga.application.api.dto.EducationLineResponse;
import local.sop.datawarehouse.message.saga.application.infrastructure.educationline.EducationLineHttpAdapter;

@ExtendWith(MockitoExtension.class)
public class EducationLineHttpAdapterTest {

	@Mock(name = "educationLine")
	private RestClient educationLineClient;

	@Mock
	RestClient.RequestHeadersUriSpec<?> mockRequestHeadersUriSpec;

	private EducationLineHttpAdapter educationLineAdapter;

	@BeforeEach
	void setUp() {
		educationLineAdapter = new EducationLineHttpAdapter(educationLineClient);
	}

	@Test
	void educationLine_findByEducationRef_shouldReturnListFromDownstream() {
		UUID id = UUID.randomUUID();
		var expected = List.of(new EducationLineResponse(
				UUID.randomUUID(), // id
				"Line A", // name
				1, // durationYears
				0, // durationMonths
				0, // durationDays
				UUID.randomUUID(), // educationRef
				Instant.now(), // createdAt
				true));

		var mockRequestHeaderSpec = mock(RestClient.RequestHeadersSpec.class);
		var mockResponseSpec = mock(RestClient.ResponseSpec.class);

		doReturn(mockRequestHeadersUriSpec).when(educationLineClient).get();
		doReturn(mockRequestHeaderSpec).when(mockRequestHeadersUriSpec).uri("/internal/educationlines/{id}/education-ref", id);
		when(mockRequestHeaderSpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(new ParameterizedTypeReference<List<EducationLineResponse>>() {})).thenReturn(expected);

		List<EducationLineResponse> result = educationLineAdapter.findByEducationRef(id);

		assertEquals(expected, result);
		verify(educationLineClient).get();
		verify(mockRequestHeadersUriSpec).uri("/internal/educationlines/{id}/education-ref", id);
		verify(mockResponseSpec).body(new ParameterizedTypeReference<List<EducationLineResponse>>() {});
	}
}
