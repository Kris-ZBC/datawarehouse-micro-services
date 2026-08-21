package local.sop.datawarehouse.registration.saga.application.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.infrastructure.security.config.MtlsClientFactory;

@ExtendWith(MockitoExtension.class)
class ServiceClientConfigTest {

    @Mock
    private MtlsClientFactory clientFactory;

    @Mock
    private RestClient restClient;

    @Mock
    private ApprenticeProps apprenticeProps;
    @Mock
    private AuditlogProps auditlogProps;
    @Mock
    private ConsentProps consentProps;
    @Mock
    private ConsentSagaProps consentSagaProps;
    @Mock
    private EducationLineProps educationLineProps;
    @Mock
    private InstructorProps instructorProps;
    @Mock
    private LoginProps loginProps;
    @Mock
    private OrganizationProps organizationProps;
    @Mock
    private PersonProps personProps;

    private ServiceClientConfig config;

    @BeforeEach
    void setup() {
        config = new ServiceClientConfig();
    }

    @Test
    void allBeansShouldCallFactoryWithCorrectArguments() {
        // Arrange
        when(apprenticeProps.baseUrl()).thenReturn("A");
        when(auditlogProps.baseurl()).thenReturn("B");
        when(consentProps.baseUrl()).thenReturn("C");
        when(consentSagaProps.baseUrl()).thenReturn("CS");
        when(educationLineProps.baseUrl()).thenReturn("D");
        when(instructorProps.baseUrl()).thenReturn("E");
        when(loginProps.baseUrl()).thenReturn("F");
        when(organizationProps.baseUrl()).thenReturn("G");
        when(personProps.baseUrl()).thenReturn("H");

        when(clientFactory.createMtlsClient(anyString(), anyString()))
                .thenReturn(restClient);

        // Act
        config.apprentice(clientFactory, apprenticeProps);
        config.auditlog(clientFactory, auditlogProps);
        config.consent(clientFactory, consentProps);
        config.consentSaga(clientFactory, consentSagaProps);
        config.educationline(clientFactory, educationLineProps);
        config.instructor(clientFactory, instructorProps);
        config.login(clientFactory, loginProps);
        config.organization(clientFactory, organizationProps);
        config.person(clientFactory, personProps);

        // Assert
        verify(clientFactory).createMtlsClient("apprentice", "A");
        verify(clientFactory).createMtlsClient("auditlog", "B");
        verify(clientFactory).createMtlsClient("consent", "C");
        verify(clientFactory).createMtlsClient("consent-saga", "CS");
        verify(clientFactory).createMtlsClient("educationline", "D");
        verify(clientFactory).createMtlsClient("instructor", "E");
        verify(clientFactory).createMtlsClient("login", "F");
        verify(clientFactory).createMtlsClient("organization", "G");
        verify(clientFactory).createMtlsClient("person", "H");

        verifyNoMoreInteractions(clientFactory);
    }
}
