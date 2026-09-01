package local.sop.datawarehouse.gateway.common.handlers.organisation.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.infrastructure.security.config.MtlsClientFactory;

@Configuration
public class OrganisationConfig {

    @Bean
    RestClient organisationClient(MtlsClientFactory mtlsClientFactory, OrganisationProps organisationProps) {
        return mtlsClientFactory.createMtlsClient("organisation", organisationProps.baseUrl());
    }
}
