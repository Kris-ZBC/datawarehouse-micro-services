package local.sop.datawarehouse.gateway.admin.handlers.user.application.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.infrastructure.security.config.MtlsClientFactory;

@Configuration
@EnableConfigurationProperties(RegistrationSagaProps.class)
public class RegistrationSagaConfig {

    @Bean
    RestClient regisration(MtlsClientFactory mtlsClientFactory, RegistrationSagaProps registrationSagaProps) {
        return mtlsClientFactory.createMtlsClient("registration", registrationSagaProps.baseUrl());
    }
}
