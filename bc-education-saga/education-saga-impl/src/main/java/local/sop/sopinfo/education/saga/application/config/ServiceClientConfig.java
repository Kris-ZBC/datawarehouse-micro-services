package local.sop.sopinfo.education.saga.application.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.infrastructure.security.config.MtlsClientFactory;

@Configuration
@EnableConfigurationProperties({EducationProps.class, EducationInstructorProps.class, AuditlogProps.class})
public class ServiceClientConfig {

    @Bean
    public RestClient educationRestClient(EducationProps props, MtlsClientFactory mtlsClientFactory) {
        return mtlsClientFactory.createMtlsClient("education", props.baseUrl());
    }

    @Bean
    public RestClient educationInstructorRestClient(EducationInstructorProps props, MtlsClientFactory mtlsClientFactory) {
        return mtlsClientFactory.createMtlsClient("education-instructor", props.baseUrl());
    }
    
    @Bean
    public RestClient auditlogRestClient(AuditlogProps props, MtlsClientFactory mtlsClientFactory) {
        return mtlsClientFactory.createMtlsClient("auditlog", props.baseUrl());
    }
}