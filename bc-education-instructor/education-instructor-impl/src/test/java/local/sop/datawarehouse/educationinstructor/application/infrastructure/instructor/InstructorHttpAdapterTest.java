package local.sop.datawarehouse.educationinstructor.application.infrastructure.instructor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

import local.sop.datawarehouse.educationinstructor.application.infrastructure.response.InstructorResponse;

@ExtendWith(MockitoExtension.class)
public class InstructorHttpAdapterTest {

    @Mock(name = "instructor")
    private RestClient instructorClient;

    @InjectMocks
    private InstructorHttpAdapter adapter;

    @Test
    @SuppressWarnings("unchecked")
    public void findById_shouldReturnInstructor_whenExists() {
        UUID id = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();
        InstructorResponse expectedResponse = new InstructorResponse(id, personRef);

        var mockRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        var mockRequestHeadersSpec    = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec          = mock(RestClient.ResponseSpec.class);
        var mockResponseEntity        = mock(ResponseEntity.class);

        when(instructorClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri("/internal/instructors/{id}", id))
                .thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.toEntity(InstructorResponse.class))
                .thenReturn(mockResponseEntity);
        when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockResponseEntity.getBody()).thenReturn(expectedResponse);

        Optional<InstructorResponse> result = adapter.findById(id);

        assertTrue(result.isPresent());
        assertTrue(adapter.exists(id));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void findById_shouldReturnEmpty_whenNotFound() {
        UUID id = UUID.randomUUID();

        var mockRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        var mockRequestHeadersSpec    = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec          = mock(RestClient.ResponseSpec.class);
        var mockResponseEntity        = mock(ResponseEntity.class);

        when(instructorClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri("/internal/instructors/{id}", id))
                .thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.toEntity(InstructorResponse.class))
                .thenReturn(mockResponseEntity);
        when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);

        Optional<InstructorResponse> result = adapter.findById(id);

        assertFalse(result.isPresent());
        assertFalse(adapter.exists(id));
    }

    @Test
    @SuppressWarnings("unchecked")
    void findById_shouldReturnEmpty_whenExceptionIsThrown() {
        UUID id = UUID.randomUUID();

        var mockRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        var mockRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(instructorClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri("/internal/instructors/{id}", id))
                .thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.toEntity(InstructorResponse.class)).thenThrow(new RuntimeException());

        Optional<InstructorResponse> result = adapter.findById(id);

        assertFalse(result.isPresent());
        assertFalse(adapter.exists(id));
    }
}