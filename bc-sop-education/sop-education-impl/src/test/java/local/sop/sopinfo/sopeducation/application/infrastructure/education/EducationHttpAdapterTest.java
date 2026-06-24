package local.sop.sopinfo.sopeducation.application.infrastructure.education;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.sopeducation.application.infrastructure.response.EducationResponse;

@ExtendWith(MockitoExtension.class)
public class EducationHttpAdapterTest {

    @Mock(name = "education")
    private RestClient educationClient;

    @InjectMocks
    private EducationHttpAdapter adapter;


    @Test
    @SuppressWarnings("unchecked")
    public void findById_shouldReturnEducation_whenExists() {
        UUID id = UUID.randomUUID();
        EducationResponse expectedResponse = new EducationResponse(id, "IT-Security", "IT", true);

        var mockRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        var mockRequestHeadersSpec    = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec          = mock(RestClient.ResponseSpec.class);
        var mockResponseEntity        = mock(ResponseEntity.class);



        when(educationClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri("/internal/educations/{id}", id))
                .thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.toEntity(EducationResponse.class))
                .thenReturn(mockResponseEntity);
        when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockResponseEntity.getBody()).thenReturn(expectedResponse);

        boolean exists = adapter.exists(id);
        verify(mockRequestHeadersUriSpec).uri("/internal/educations/{id}", id);
        verify(mockRequestHeadersSpec).retrieve();
        assertTrue(exists);
    }


    @Test
    @SuppressWarnings("unchecked")
    public void findById_shouldReturnEmpty_whenNotFound() {
        UUID id = UUID.randomUUID();

        var mockRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        var mockRequestHeadersSpec    = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec          = mock(RestClient.ResponseSpec.class);
        var mockResponseEntity        = mock(ResponseEntity.class);

        when(educationClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri("/internal/educations/{id}", id))
                .thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.toEntity(EducationResponse.class))
                .thenReturn(mockResponseEntity);
        when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);

        boolean exists = adapter.exists(id);
        assertFalse(exists);
        verify(mockRequestHeadersUriSpec).uri("/internal/educations/{id}", id);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findById_shouldReturnEmpty_whenExceptionIsThrown() {
        UUID id = UUID.randomUUID();

        var mockRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        var mockRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(educationClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri("/internal/educations/{id}", id))
                .thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.toEntity(EducationResponse.class))
                .thenThrow(new RuntimeException("Connection refused"));

        boolean exists = adapter.exists(id);

        assertFalse(exists);
        verify(educationClient).get();
        verify(mockRequestHeadersUriSpec).uri("/internal/educations/{id}", id);
    }
}
