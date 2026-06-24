package local.sop.sopinfo.sopeducation.application.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.infrastructure.security.config.MtlsClientFactory;

@ExtendWith(MockitoExtension.class)
public class ServiceClientConfigTest {
    @Mock
    private MtlsClientFactory clientFactory;

    @Mock
    private RestClient restClient;

    private static final String SOP_BASE_URL       = "https://bc-sop.sop.local:9453";
    private static final String EDUCATION_BASE_URL  = "https://bc-education.sop.local:9459";

    private final ServiceClientConfig config = new ServiceClientConfig();

    // ── education bean ─────────────────────────────────────────────────────────

    @Test
    void education_shouldCreateRestClient_withCorrectBaseUrl() {
        EducationProps props = new EducationProps(EDUCATION_BASE_URL);
        when(clientFactory.createMtlsClient("education", EDUCATION_BASE_URL)).thenReturn(restClient);

        RestClient result = config.education(clientFactory, props);

        assertNotNull(result);
        verify(clientFactory).createMtlsClient("education", EDUCATION_BASE_URL);
    }

    @Test
    void education_shouldUseCorrectBeanQualifier() {
        // Verify the default base URL matches application.properties default
        EducationProps props = new EducationProps(EDUCATION_BASE_URL);
        when(clientFactory.createMtlsClient(any(), any())).thenReturn(restClient);

        config.education(clientFactory, props);

        verify(clientFactory).createMtlsClient(eq("education"), eq(EDUCATION_BASE_URL));
    }

    @Test
    void education_shouldPassBaseUrl_fromProps() {
        String customUrl = "https://custom-education.sop.local:9999";
        EducationProps props = new EducationProps(customUrl);
        when(clientFactory.createMtlsClient("education", customUrl)).thenReturn(restClient);

        RestClient result = config.education(clientFactory, props);

        assertNotNull(result);
        verify(clientFactory).createMtlsClient(eq("education"), eq(customUrl));
    }

    // ── sop bean ───────────────────────────────────────────────────────────────

    @Test
    void sop_shouldCreateRestClient_withCorrectBaseUrl() {
        SopProps props = new SopProps(SOP_BASE_URL);
        when(clientFactory.createMtlsClient("sop", SOP_BASE_URL)).thenReturn(restClient);

        RestClient result = config.sop(clientFactory, props);

        assertNotNull(result);
        verify(clientFactory).createMtlsClient("sop", SOP_BASE_URL);
    }

    @Test
    void sop_shouldUseCorrectBeanQualifier() {
        SopProps props = new SopProps(SOP_BASE_URL);
        when(clientFactory.createMtlsClient(any(), any())).thenReturn(restClient);

        config.sop(clientFactory, props);

        verify(clientFactory).createMtlsClient(eq("sop"), eq(SOP_BASE_URL));
    }

    @Test
    void sop_shouldPassBaseUrl_fromProps() {
        String customUrl = "https://custom-sop.sop.local:9999";
        SopProps props = new SopProps(customUrl);
        when(clientFactory.createMtlsClient("sop", customUrl)).thenReturn(restClient);

        RestClient result = config.sop(clientFactory, props);

        assertNotNull(result);
        verify(clientFactory).createMtlsClient(eq("sop"), eq(customUrl));
    }

    // ── default URLs match application.properties ──────────────────────────────

    @Test
    void educationProps_defaultBaseUrl_shouldMatchApplicationProperties() {
        // Verifies the default defined in application.properties is the expected value
        // If this changes, it signals a potentially breaking configuration change
        assertEquals("https://bc-education.sop.local:9459", EDUCATION_BASE_URL);
    }

    @Test
    void sopProps_defaultBaseUrl_shouldMatchApplicationProperties() {
        assertEquals("https://bc-sop.sop.local:9453", SOP_BASE_URL);
    }
}
