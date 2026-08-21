package local.sop.datawarehouse.gateway.common.handlers.sop.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.infrastructure.security.config.MtlsClientFactory;

@Configuration
public class SopConfig {
    @Bean
    RestClient sopClient(MtlsClientFactory mtlsClientFactory, SopProps sopProps) {
        return mtlsClientFactory.createMtlsClient("sop", sopProps.baseUrl());
    }
}
