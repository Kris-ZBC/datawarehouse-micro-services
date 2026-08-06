package local.sop.datawarehouse.educationline.domain.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EducationLineDomainConfig {
	@Bean
	EducationLineDomain educationLineDomain() {
		return new EducationLineDomainService();
	}
}
