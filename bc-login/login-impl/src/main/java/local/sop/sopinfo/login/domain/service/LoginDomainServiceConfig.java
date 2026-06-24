package local.sop.sopinfo.login.domain.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoginDomainServiceConfig {

	@Bean
	public LoginDomain loginDomain() {
		return new LoginDomainService();
	}
}
