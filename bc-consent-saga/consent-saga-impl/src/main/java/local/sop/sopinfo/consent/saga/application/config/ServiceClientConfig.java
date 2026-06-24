package local.sop.sopinfo.consent.saga.application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import local.sop.sopinfo.infrastructure.security.config.MtlsClientFactory;

@Configuration
@EnableConfigurationProperties({ConsentProps.class, AuditlogProps.class})
public class ServiceClientConfig {

    @Bean("consent")
    RestClient consent(MtlsClientFactory clientFactory, ConsentProps consentProps) {
        return clientFactory.createMtlsClient("consent", consentProps.baseUrl());
    }

    @Bean("auditlog")
    RestClient auditlog(MtlsClientFactory clientFactory, AuditlogProps auditlogProps) {
        return clientFactory.createMtlsClient("auditlog", auditlogProps.baseUrl());
    }
    

}
