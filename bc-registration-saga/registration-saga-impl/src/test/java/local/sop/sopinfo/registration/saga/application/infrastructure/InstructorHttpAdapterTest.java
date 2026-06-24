package local.sop.sopinfo.registration.saga.application.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
import local.sop.sopinfo.registration.saga.application.api.dto.instructor.CreateInstructorCmd;
import local.sop.sopinfo.registration.saga.application.api.dto.instructor.InstructorResponse;
import local.sop.sopinfo.registration.saga.application.infrastructure.instructor.InstructorHttpAdapter;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.sopinfo.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;

@ExtendWith(MockitoExtension.class)
class InstructorHttpAdapterTest {
    /*
     * ----------------------------------------------------------- *
     * InstructorHttpAdapter tests
     * -----------------------------------------------------------
     */

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
    void instructor_create_shouldReturnUuidFromDownstream() {
        UUID expectedId = UUID.randomUUID();
        var request = new CreateInstructorCmd(UUID.randomUUID());

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(instructorClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/instructors")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(CreateInstructorCmd.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(UUID.class)).thenReturn(expectedId);

        UUID result = instructorAdapter.create(request);
        assertEquals(expectedId, result);
        verify(instructorClient).post();
        verify(mockRequestBodyUriSpec).uri("/internal/instructors");
        verify(mockRequestBodySpec).body(request);
        verify(mockResponseSpec).body(UUID.class);
    }

    @Test
    void instructor_compensate_shouldReturnResultFromDownstream() {
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        var request = new PayloadCompensateCreate(InstructorHttpAdapter.class, state);
        var expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(instructorClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/instructors/{id}/compensate/create", id))
                .thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadCompensateCreate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

        ResponseCompensated result = instructorAdapter.compensate(id, InstructorHttpAdapter.class, state);

        assertEquals(expectedResult.sagaState(), result.sagaState());
        assertEquals(expectedResult.success(), result.success());
        verify(instructorClient).put();
        verify(mockRequestBodyUriSpec).uri("/internal/instructors/{id}/compensate/create", id);
        verify(mockRequestBodySpec).body(request);
        verify(mockResponseSpec).body(ResponseCompensated.class);
    }

    @Test
    void instructor_get_shouldReturnInstructorFromDownstream() {
        UUID id = UUID.randomUUID();
        var expected = new InstructorResponse(id, UUID.randomUUID());
        var mockRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        doReturn(mockRequestHeadersUriSpec).when(instructorClient).get();
        doReturn(mockRequestHeadersSpec).when(mockRequestHeadersUriSpec)
                .uri("/internal/instructors/{id}", id);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(InstructorResponse.class)).thenReturn(expected);

        InstructorResponse result = instructorAdapter.getById(id);

        assertEquals(expected, result);
        verify(instructorClient).get();
        verify(mockRequestHeadersUriSpec).uri("/internal/instructors/{id}", id);
        verify(mockResponseSpec).body(InstructorResponse.class);
    }
}
