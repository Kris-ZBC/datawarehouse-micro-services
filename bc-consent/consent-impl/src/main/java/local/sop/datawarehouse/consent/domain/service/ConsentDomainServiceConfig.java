package local.sop.datawarehouse.consent.domain.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ConsentDomainServiceConfig {

    @Bean
    ConsentDomain workHourScheduleDomain() {
        return new ConsentDomainService();
    }
}
