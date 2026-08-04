package local.sop.sopinfo.login.saga.application.config;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
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
    private RestClient loginClient;

    @Mock
    private RestClient consentClient;

    @Mock
    private RestClient auditlogClient;

    private ServiceClientConfig config;

    @BeforeEach
    void setUp() {
        config = new ServiceClientConfig();
    }

    @Test
    void login_shouldCreateMtlsClientWithLoginServiceName() {
        LoginProps props = new LoginProps("https://login.svc.local");
        when(clientFactory.createMtlsClient("login", props.baseUrl())).thenReturn(loginClient);

        RestClient result = config.login(clientFactory, props);

        assertSame(loginClient, result);
        verify(clientFactory).createMtlsClient("login", props.baseUrl());
    }

    @Test
    void consent_shouldCreateMtlsClientWithConsentServiceName() {
        ConsentProps props = new ConsentProps("https://consent.svc.local");
        when(clientFactory.createMtlsClient("consent", props.baseUrl())).thenReturn(consentClient);

        RestClient result = config.consent(clientFactory, props);

        assertSame(consentClient, result);
        verify(clientFactory).createMtlsClient("consent", props.baseUrl());
    }

    @Test
    void auditlog_shouldCreateMtlsClientWithAuditlogServiceName() {
        AuditlogProps props = new AuditlogProps("https://auditlog.svc.local");
        when(clientFactory.createMtlsClient("auditlog", props.baseurl())).thenReturn(auditlogClient);

        RestClient result = config.auditlog(clientFactory, props);

        assertSame(auditlogClient, result);
        verify(clientFactory).createMtlsClient("auditlog", props.baseurl());
    }
}
