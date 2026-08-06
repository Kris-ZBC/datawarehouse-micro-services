package local.sop.datawarehouse.sopeducation.domain.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SopEducationDomainConfig {

        @Bean
        SopEducationDomain sopEducationDomain() {
            return new SopEducationDomainService();
        }
}
