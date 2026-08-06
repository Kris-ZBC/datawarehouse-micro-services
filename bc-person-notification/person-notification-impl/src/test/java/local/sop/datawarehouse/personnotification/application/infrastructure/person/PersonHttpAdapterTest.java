package local.sop.datawarehouse.personnotification.application.infrastructure.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.sharedkernel.enums.PhoneUserType;
import local.sop.datawarehouse.personnotification.application.infrastructure.response.PersonResponse;
import local.sop.datawarehouse.personnotification.application.infrastructure.response.PhoneNumberResponse;

@ExtendWith(MockitoExtension.class)
public class PersonHttpAdapterTest {

    @Mock(name = "person")
    private RestClient personClient;

    @InjectMocks
    private PersonHttpAdapter adapter;

    @Test
    @SuppressWarnings("unchecked")
    public void findById_shouldReturnPerson_whenExists() {
        UUID id = UUID.randomUUID();
        UUID organizationRef = UUID.randomUUID();
        List<PhoneNumberResponse> phoneNumbers = List.of(new PhoneNumberResponse(UUID.randomUUID(),PhoneUserType.SELF,"12345678"));

        PersonResponse expectedResponse = new PersonResponse(id, "John", "Doe", "mail@example.com", organizationRef, phoneNumbers);

        var mockRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        var mockRequestHeadersSpec    = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec          = mock(RestClient.ResponseSpec.class);
        var mockResponseEntity        = mock(ResponseEntity.class);

        when(personClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri("/internal/persons/{id}", id))
                .thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.toEntity(PersonResponse.class))
                .thenReturn(mockResponseEntity);
        when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockResponseEntity.getBody()).thenReturn(expectedResponse);

        boolean exists = adapter.exists(id);

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

        when(personClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri("/internal/persons/{id}", id))
                .thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.toEntity(PersonResponse.class))
                .thenReturn(mockResponseEntity);
        when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);

        boolean exists = adapter.exists(id);

        assertFalse(exists);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findById_shouldReturnEmpty_whenExceptionIsThrown() {
        UUID id = UUID.randomUUID();

        var mockRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        var mockRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(personClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri("/internal/persons/{id}", id))
                .thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.toEntity(PersonResponse.class)).thenThrow(new RuntimeException());

        boolean exists = adapter.exists(id);

        assertFalse(exists);
        verify(personClient).get();
    }

}
