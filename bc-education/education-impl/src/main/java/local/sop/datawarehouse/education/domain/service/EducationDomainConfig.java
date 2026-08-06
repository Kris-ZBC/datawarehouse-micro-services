package local.sop.datawarehouse.education.domain.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EducationDomainConfig {
    @Bean
    public
    EducationDomain educationDomain() {
        return new EducationDomainService();
    }
}