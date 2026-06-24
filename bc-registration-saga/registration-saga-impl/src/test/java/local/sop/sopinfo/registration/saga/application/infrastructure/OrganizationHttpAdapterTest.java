package local.sop.sopinfo.registration.saga.application.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.registration.saga.application.api.dto.organization.OrganisationResponse;
import local.sop.sopinfo.registration.saga.application.infrastructure.organization.OrganizationHttpAdapter;

@ExtendWith(MockitoExtension.class)
class OrganizationHttpAdapterTest {
    /*
     * ----------------------------------------------------------- *
     * OrganizationHttpAdapterTest tests
     * -----------------------------------------------------------
     */

    @Mock(name = "organization")
    private RestClient organizationClient;

    @Mock
    RestClient.RequestHeadersUriSpec<?> mockRequestHeadersUriSpec;

    private OrganizationHttpAdapter organizationAdapter;

    @BeforeEach
    void setUp() {
        organizationAdapter = new OrganizationHttpAdapter(organizationClient);
    }

    @Test
    void organization_get_shouldReturnOrganizationFromDownstream() {
        UUID id = UUID.randomUUID();
        var expected = new OrganisationResponse(id, "some name","");
        var mockRequestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        var mockResponseSpec = mock(RestClient.ResponseSpec.class);

        doReturn(mockRequestHeadersUriSpec).when(organizationClient).get();
        doReturn(mockRequestHeadersSpec).when(mockRequestHeadersUriSpec)
                .uri("/internal/organizations?id={id}", id);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(OrganisationResponse.class)).thenReturn(expected);

        OrganisationResponse result = organizationAdapter.getById(id);

        assertEquals(expected, result);
        verify(organizationClient).get();
        verify(mockRequestHeadersUriSpec).uri("/internal/organizations?id={id}", id);
        verify(mockResponseSpec).body(OrganisationResponse.class);
    }

}
