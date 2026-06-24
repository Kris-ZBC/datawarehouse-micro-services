package local.sop.sopinfo.sopinstructor.domain.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SopInstructorDomainConfig {
    @Bean
    SopInstructorDomain sopInstructorDomain() {
        return new SopInstructorDomainService();
    }

}
