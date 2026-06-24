package local.sop.sopinfo.registration.saga.application.infrastructure;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import local.sop.sopinfo.registration.saga.application.api.dto.educationline.EducationLineResponse;
import local.sop.sopinfo.registration.saga.application.infrastructure.educationline.EducationLineHttpAdapter;

@ExtendWith(MockitoExtension.class)
class EducationLineHttpAdapterTest {
    /*
     * ----------------------------------------------------------- *
     * EducationLineHttpAdapterTest tests
     * -----------------------------------------------------------
     */

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
    void educationLine_get_shouldReturnEducationLineFromDownstream() {
        UUID id = UUID.randomUUID();
        var expected = new EducationLineResponse(id, "Computer Science", 3, 0, 0, UUID.randomUUID(), null, true);
        var mockRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        doReturn(mockRequestHeadersUriSpec).when(educationLineClient).get();
        doReturn(mockRequestHeadersSpec).when(mockRequestHeadersUriSpec)
                .uri("/internal/educationlines/{id}", id);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationLineResponse.class)).thenReturn(expected);

        EducationLineResponse result = educationLineAdapter.getById(id);

        assertEquals(expected, result);
        verify(educationLineClient).get();
        verify(mockRequestHeadersUriSpec).uri("/internal/educationlines/{id}", id);
        verify(mockResponseSpec).body(EducationLineResponse.class);
    }

}
