package local.sop.datawarehouse.educationline.saga.application.infrastructure.education;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;
import org.springframework.web.client.RestClient.ResponseSpec;

import local.sop.datawarehouse.educationline.saga.application.api.dto.EducationResponse;

@ExtendWith(MockitoExtension.class)
class EducationHttpAdapterTest {

    private final RestClient educationClient = mock(RestClient.class);
    private final EducationHttpAdapter adapter = new EducationHttpAdapter(educationClient);

    @SuppressWarnings("unchecked")
    @Test
    void existsById_shouldReturnEducationResponseFromDownstream() {
        // Given
        UUID id = UUID.randomUUID();

        EducationResponse expected = mock(EducationResponse.class);

		@SuppressWarnings("rawtypes")
        RequestHeadersUriSpec requestSpec = mock(RequestHeadersUriSpec.class);
        ResponseSpec responseSpec = mock(ResponseSpec.class);
		
        // When
        when(educationClient.get()).thenReturn(requestSpec);
        when(requestSpec.uri("/internal/educations/{id}", id)).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(EducationResponse.class)).thenReturn(expected);

        EducationResponse actual = adapter.existsById(id);

        // Then
        assertEquals(expected, actual);

        verify(educationClient).get();
        verify(requestSpec).uri("/internal/educations/{id}", id);
        verify(requestSpec).retrieve();
        verify(responseSpec).body(EducationResponse.class);
    }
}