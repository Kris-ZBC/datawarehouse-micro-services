package local.sop.datawarehouse.notification.domain.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class NotificationDomainServiceConfig {

	@Bean
	public NotificationDomain notificationDomain() {
		return new NotificationDomainService();
	}
}
