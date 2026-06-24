package local.sop.sopinfo.anonymize.saga.application.infrastructure.person;

import java.util.List;
import java.util.UUID;

import local.sop.sopinfo.person.application.api.dto.PersonResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonHttpAdapterTest {

    @Mock
    private RestClient restClient;

    @SuppressWarnings("rawtypes")
    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    
    @SuppressWarnings("rawtypes")
    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;
    
    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private PersonHttpAdapter adapter;


    @Test
    @SuppressWarnings("unchecked")
    void get_ShouldCallExpectedEndpointAndReturnPerson() {
        UUID personRef = UUID.randomUUID();
        PersonResponse expected = new PersonResponse(
                personRef,
                "Jane",
                "Doe",
                "jane.doe@example.com",
                UUID.randomUUID(),
                List.of()
        );

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq("/internal/persons/{personRef}"), eq(personRef))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(PersonResponse.class)).thenReturn(expected);

        PersonResponse result = adapter.get(personRef);

        assertSame(expected, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void get_ShouldThrowRuntimeException() {
        UUID personRef = UUID.randomUUID();

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq("internal/persons/{personRef}"), eq(personRef))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenThrow(new RuntimeException("downstream failed"));

        assertThrows(RuntimeException.class, () -> adapter.get(personRef));
    }
}
