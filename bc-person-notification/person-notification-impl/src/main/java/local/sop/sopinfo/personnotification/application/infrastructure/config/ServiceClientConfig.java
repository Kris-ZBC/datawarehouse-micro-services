package local.sop.sopinfo.personnotification.application.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.infrastructure.security.config.MtlsClientFactory;

@Configuration
@EnableConfigurationProperties({PersonProps.class, NotificationProps.class})
public class ServiceClientConfig {

    @Bean("person")
    RestClient person(MtlsClientFactory clientFactory, PersonProps personProps) {
        return clientFactory.createMtlsClient("person", personProps.baseUrl());
    }

    @Bean("notification")
    RestClient notification(MtlsClientFactory clientFactory, NotificationProps notificationProps) {
        return clientFactory.createMtlsClient("notification", notificationProps.baseUrl());
    }
    
}
