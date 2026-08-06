package local.sop.datawarehouse.workhour.domain.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class WorkHourDomainServiceConfig {

    @Bean
    WorkHourDomain workHourDomain() {
        return new WorkHourDomainService();
    }
}