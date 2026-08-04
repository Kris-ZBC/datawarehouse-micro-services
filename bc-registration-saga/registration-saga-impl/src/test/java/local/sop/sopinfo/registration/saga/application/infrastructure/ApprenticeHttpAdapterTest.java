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

import local.sop.sopinfo.registration.saga.application.api.dto.apprentice.ApprenticeResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.apprentice.CreateApprenticeCmd;
import local.sop.sopinfo.registration.saga.application.infrastructure.apprentice.ApprenticeHttpAdapter;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;

@ExtendWith(MockitoExtension.class)
class ApprenticeHttpAdapterTest {
    
        /*
     * ----------------------------------------------------------- *
     * ApprenticeHttpAdapter tests
     * -----------------------------------------------------------
     */

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
    void apprentice_create_shouldReturnUuidFromDownstream() {
        UUID expectedId = UUID.randomUUID();
        var request = new CreateApprenticeCmd(UUID.randomUUID(), UUID.randomUUID());

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(apprenticeClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/apprentices")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(CreateApprenticeCmd.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(UUID.class)).thenReturn(expectedId);

        UUID result = apprenticeAdapter.create(request);
        assertEquals(expectedId, result);
        verify(apprenticeClient).post();
        verify(mockRequestBodyUriSpec).uri("/internal/apprentices");
        verify(mockRequestBodySpec).body(request);
        verify(mockResponseSpec).body(UUID.class);
    }

    @Test
    void apprentice_compensate_shouldReturnResultFromDownstream() {
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        var request = new PayloadCompensateCreate(ApprenticeHttpAdapter.class, state);
        var expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(apprenticeClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/apprentices/{id}/compensate/create", id))
                .thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadCompensateCreate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

        ResponseCompensated result = apprenticeAdapter.compensate(id, ApprenticeHttpAdapter.class, state);

        assertEquals(expectedResult.sagaState(), result.sagaState());
        assertEquals(expectedResult.success(), result.success());
        verify(apprenticeClient).put();
        verify(mockRequestBodyUriSpec).uri("/internal/apprentices/{id}/compensate/create", id);
        verify(mockRequestBodySpec).body(request);
        verify(mockResponseSpec).body(ResponseCompensated.class);
    }

    @Test
    void apprentice_get_shouldReturnApprenticeFromDownstream() {
        UUID id = UUID.randomUUID();
        var expected = new ApprenticeResponse(id, UUID.randomUUID(), UUID.randomUUID());
        var mockRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        doReturn(mockRequestHeadersUriSpec).when(apprenticeClient).get();
        doReturn(mockRequestHeadersSpec).when(mockRequestHeadersUriSpec)
                .uri("/internal/apprentices/{id}", id);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ApprenticeResponse.class)).thenReturn(expected);

        ApprenticeResponse result = apprenticeAdapter.getById(id);

        assertEquals(expected, result);
        verify(apprenticeClient).get();
        verify(mockRequestHeadersUriSpec).uri("/internal/apprentices/{id}", id);
        verify(mockResponseSpec).body(ApprenticeResponse.class);
    }
}
