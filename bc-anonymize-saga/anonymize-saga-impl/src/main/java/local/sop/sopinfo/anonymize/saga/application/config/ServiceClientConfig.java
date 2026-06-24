package local.sop.sopinfo.anonymize.saga.application.config;

import local.sop.sopinfo.infrastructure.security.config.MtlsClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({PersonProps.class, AnonymizeProps.class, AuditlogProps.class})
public class ServiceClientConfig {
	@Bean("anonymize")
	RestClient anonymize(MtlsClientFactory clientFactory, AnonymizeProps anonymizeProps){
		return clientFactory.createMtlsClient("anonymize", anonymizeProps.baseUrl());
	}

	@Bean("person")
	RestClient person(MtlsClientFactory clientFactory, PersonProps personProps){
		return clientFactory.createMtlsClient("person", personProps.baseUrl());
	}

    @Bean("auditlog")
    RestClient auditlog(MtlsClientFactory clientFactory, AuditlogProps auditlogProps) {
        return clientFactory.createMtlsClient("auditlog", auditlogProps.baseUrl());
    }
}
