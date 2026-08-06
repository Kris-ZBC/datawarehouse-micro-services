package local.sop.datawarehouse.personnotification.domain.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class PersonNotificationDomainConfig {

        @Bean
        PersonNotificationDomain personNotificationDomain() {
            return new PersonNotificationDomainService();
        }
}

