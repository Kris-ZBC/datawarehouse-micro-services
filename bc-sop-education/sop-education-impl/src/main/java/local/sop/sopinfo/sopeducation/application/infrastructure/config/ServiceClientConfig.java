package local.sop.sopinfo.sopeducation.application.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import local.sop.common.libs.infrastructure.security.config.MtlsClientFactory;

@Configuration
@EnableConfigurationProperties({EducationProps.class, SopProps.class})
public class ServiceClientConfig {

    @Bean("education")
    RestClient education(MtlsClientFactory clientFactory, EducationProps educationProps) {
        return clientFactory.createMtlsClient("education", educationProps.baseUrl());
    }

    @Bean("sop")
    RestClient sop(MtlsClientFactory clientFactory, SopProps sopProps) {
        return clientFactory.createMtlsClient("sop", sopProps.baseUrl());
    }
    

}
