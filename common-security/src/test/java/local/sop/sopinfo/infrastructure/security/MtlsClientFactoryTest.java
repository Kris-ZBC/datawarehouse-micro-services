package local.sop.sopinfo.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.infrastructure.security.config.MtlsClientFactory;

@ExtendWith(MockitoExtension.class)
public class MtlsClientFactoryTest {

    @Mock private SslBundles sslBundles;

    @Mock
    private SslBundle sslBundle;

    private MtlsClientFactory factory;

    @BeforeEach
    void setUp() {
        // Fabrikken bruger altid bundle‑navnet “app”
        when(sslBundles.getBundle("app")).thenReturn(sslBundle);
        factory = new MtlsClientFactory(sslBundles);
    }

    /**
     * Verificerer at fabrikken kan oprette en {@link RestClient} og at den
     * rigtige SSL‑bundle bliver hentet.
     */
    @Test
    void shouldCreateMtlsClient_WithValidParameters() {
        String serviceName = "test-service";
        String baseUrl = "https://api.example.com";

        RestClient client = factory.createMtlsClient(serviceName, baseUrl);

        assertNotNull(client, "RestClient skal ikke være null");
        verify(sslBundles).getBundle("app");
    }

    /**
     * Tester at hver gang {@link MtlsClientFactory#createMtlsClient}
     * kaldes, får man et nyt {@link RestClient}‑objekt (prototype‑scope).
     */
    @Test
    void shouldCreateNewInstanceOnEachCall() {
        String serviceName = "test-service";
        String baseUrl = "https://api.example.com";

        RestClient client1 = factory.createMtlsClient(serviceName, baseUrl);
        RestClient client2 = factory.createMtlsClient(serviceName, baseUrl);

        assertNotSame(client1, client2,
                "Hvert kald skal give et nyt RestClient‑objekt");
    }

    /**
     * Verificerer at en {@link IllegalArgumentException} bliver kastet,
     * hvis den ønskede SSL‑bundle ikke findes.
     */
    @Test
    void shouldThrowWhenBundleNotFound() {
        // Simuler at bundle’en ikke kan findes
        when(sslBundles.getBundle("app"))
                .thenThrow(new IllegalArgumentException("Bundle not found"));

        assertThrows(IllegalArgumentException.class,
                () -> factory.createMtlsClient("svc", "https://foo"));
    }

}
