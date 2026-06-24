package local.sop.sopinfo.apprentice.domain.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ApprenticeDomainServiceConfig {
    
    @Bean
    ApprenticeDomain apprenticeDomain() {
        return new ApprenticeDomainService();
    }
}
