package local.sop.sopinfo.messageperson.domain.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MessagePersonDomainConfig {
    @Bean
    MessagePersonDomain messagePersonDomain() {
        return new MessagePersonDomainService();
    }

}
