package local.sop.datawarehouse.sopinstructor.application.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import local.sop.common.libs.infrastructure.security.config.MtlsClientFactory;

@Configuration
@EnableConfigurationProperties({SopProps.class, InstructorProps.class})
public class ServiceClientConfig {

    @Bean("sop")
    RestClient sop(MtlsClientFactory clientFactory, SopProps sopProps) {
        return clientFactory.createMtlsClient("sop", sopProps.baseUrl());
    }

    @Bean("instructor")
    RestClient instructor(MtlsClientFactory clientFactory, InstructorProps instructorProps) {
        return clientFactory.createMtlsClient("instructor", instructorProps.baseUrl());
    }
    

}
