package local.sop.sopinfo.messageperson.application.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import local.sop.common.libs.infrastructure.security.config.MtlsClientFactory;

@Configuration
@EnableConfigurationProperties({MessageProps.class, PersonProps.class})
public class ServiceClientConfig {

    @Bean("message")
    RestClient message(MtlsClientFactory clientFactory, MessageProps messageProps) {
        return clientFactory.createMtlsClient("message", messageProps.baseUrl());
    }

    @Bean("person")
    RestClient person(MtlsClientFactory clientFactory, PersonProps personProps) {
        return clientFactory.createMtlsClient("person", personProps.baseUrl());
    }
    

}
