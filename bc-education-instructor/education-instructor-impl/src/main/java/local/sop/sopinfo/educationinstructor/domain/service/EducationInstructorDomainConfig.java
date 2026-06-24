package local.sop.sopinfo.educationinstructor.domain.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class EducationInstructorDomainConfig {
	@Bean
	EducationInstructorDomain educationInstructorDomain() {
		return new EducationInstructorDomainService();
	}
}
