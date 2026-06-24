package local.sop.sopinfo.infrastructure.security.config;


import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
public class MtlsClientFactory {
    
    private final SslBundles sslBundles;
    
    public MtlsClientFactory(SslBundles sslBundles) {
        this.sslBundles = sslBundles;
    }
    
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public RestClient createMtlsClient(String serviceName, String baseUrl) {
        // Correct constructor with SSL bundle
        ClientHttpRequestFactorySettings settings = new ClientHttpRequestFactorySettings(
            ClientHttpRequestFactorySettings.Redirects.FOLLOW, // redirects
            Duration.ofSeconds(30), // connectTimeout
            Duration.ofSeconds(60), // readTimeout
            sslBundles.getBundle("app")); // sslBundle
            
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder
            .detect()
            .build(settings);
        
        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .build();
    }
}