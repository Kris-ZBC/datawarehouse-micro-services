package local.sop.datawarehouse.registration.saga.application.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.web.client.RestClient;

import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.registration.saga.application.api.dto.person.CreatePersonCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.person.PersonResponse;
import local.sop.datawarehouse.registration.saga.application.infrastructure.person.PersonHttpAdapter;

@ExtendWith(MockitoExtension.class)
class PersonHttpAdapterTest {
    /*
     * ----------------------------------------------------------- *
     * PersonHttpAdapter tests
     * -----------------------------------------------------------
     */

    @Mock(name = "person")
    private RestClient personClient;

    @Mock
    RestClient.RequestHeadersUriSpec<?> mockRequestHeadersUriSpec;

    private PersonHttpAdapter personAdapter;

    @BeforeEach
    void setUp() {
        personAdapter = new PersonHttpAdapter(personClient);
    }

    @Test
    void person_create_shouldReturnUuidFromDownstream() {
        UUID expectedId = UUID.randomUUID();
        var request = new CreatePersonCmd(
                "John", "Doe", "john.doe@example.com", UUID.randomUUID(), List.of());

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(personClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/persons")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(CreatePersonCmd.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(UUID.class)).thenReturn(expectedId);

        UUID result = personAdapter.create(request);
        assertEquals(expectedId, result);
        verify(personClient).post();
        verify(mockRequestBodyUriSpec).uri("/internal/persons");
        verify(mockRequestBodySpec).body(request);
        verify(mockResponseSpec).body(UUID.class);
    }

    @Test
    void person_compensate_shouldReturnResultFromDownstream() {
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        var request = new PayloadCompensateCreate(PersonHttpAdapter.class, state);
        var expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        var mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        var mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(personClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/internal/persons/{id}/compensate/create", id))
                .thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadCompensateCreate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

        ResponseCompensated result = personAdapter.compensate(id, PersonHttpAdapter.class, state);

        assertEquals(expectedResult.sagaState(), result.sagaState());
        assertEquals(expectedResult.success(), result.success());
        verify(personClient).put();
        verify(mockRequestBodyUriSpec).uri("/internal/persons/{id}/compensate/create", id);
        verify(mockRequestBodySpec).body(request);
        verify(mockResponseSpec).body(ResponseCompensated.class);
    }

    @Test
    void person_get_shouldReturnPersonFromDownstream() {
        UUID id = UUID.randomUUID();
        var expected = new PersonResponse(id, "Daniel", "S", "daniel.s@example.com", UUID.randomUUID(), List.of());
        var mockRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        doReturn(mockRequestHeadersUriSpec).when(personClient).get();
        doReturn(mockRequestHeadersSpec).when(mockRequestHeadersUriSpec)
                .uri("/internal/persons/{id}", id);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(PersonResponse.class)).thenReturn(expected);

        PersonResponse result = personAdapter.getById(id);

        assertEquals(expected, result);
        verify(personClient).get();
        verify(mockRequestHeadersUriSpec).uri("/internal/persons/{id}", id);
        verify(mockResponseSpec).body(PersonResponse.class);
    }
}
