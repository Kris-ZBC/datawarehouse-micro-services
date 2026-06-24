package local.sop.sopinfo.workhour.domain.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class WorkHourScheduleDomainServiceConfig {

    @Bean
    WorkHourScheduleDomain workHourScheduleDomain() {
        return new WorkHourScheduleDomainService();
    }
}
