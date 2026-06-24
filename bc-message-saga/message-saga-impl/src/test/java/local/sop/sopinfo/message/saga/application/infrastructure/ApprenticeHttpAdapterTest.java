package local.sop.sopinfo.message.saga.application.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import local.sop.sopinfo.message.saga.application.api.dto.ApprenticeResponse;
import local.sop.sopinfo.message.saga.application.infrastructure.apprentice.ApprenticeHttpAdapter;

@ExtendWith(MockitoExtension.class)
public class ApprenticeHttpAdapterTest {

	@Mock(name = "apprentice")
	private RestClient apprenticeClient;

	@Mock
	RestClient.RequestHeadersUriSpec<?> mockRequestHeadersUriSpec;

	private ApprenticeHttpAdapter apprenticeAdapter;

	@BeforeEach
	void setUp() {
		apprenticeAdapter = new ApprenticeHttpAdapter(apprenticeClient);
	}

	@Test
	void apprentice_findByEducationLineRef_shouldReturnListFromDownstream() {
		UUID id = UUID.randomUUID();
		var expected = List.of(new ApprenticeResponse(UUID.randomUUID(), UUID.randomUUID(), id));

		var mockRequestHeaderSpec = mock(RestClient.RequestHeadersSpec.class);
		var mockResponseSpec = mock(RestClient.ResponseSpec.class);

		doReturn(mockRequestHeadersUriSpec).when(apprenticeClient).get();
		doReturn(mockRequestHeaderSpec).when(mockRequestHeadersUriSpec).uri("/internal/apprentices/by-education-line/{id}", id);
		when(mockRequestHeaderSpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(new ParameterizedTypeReference<List<ApprenticeResponse>>() {})).thenReturn(expected);

		List<ApprenticeResponse> result = apprenticeAdapter.findByEducationLineRef(id);

		assertEquals(expected, result);
    	verify(apprenticeClient).get();
    	verify(mockRequestHeadersUriSpec).uri("/internal/apprentices/by-education-line/{id}", id);
    	verify(mockResponseSpec).body(new ParameterizedTypeReference<List<ApprenticeResponse>>() {});
	}
}
