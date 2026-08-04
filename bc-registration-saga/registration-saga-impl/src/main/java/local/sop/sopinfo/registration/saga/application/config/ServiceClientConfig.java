package local.sop.sopinfo.registration.saga.application.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.infrastructure.security.config.MtlsClientFactory;

@Configuration
@EnableConfigurationProperties(LoginProps.class)
public class ServiceClientConfig {

	@Bean("apprentice")
	RestClient apprentice(MtlsClientFactory clientFactory, ApprenticeProps apprenticeProps) {
		return clientFactory.createMtlsClient("apprentice", apprenticeProps.baseUrl());
	}

	@Bean("auditLog")
	RestClient auditlog(MtlsClientFactory clientFactory, AuditlogProps auditLogProps) {
		return clientFactory.createMtlsClient("auditlog", auditLogProps.baseurl());
	}

	@Bean("consent")
	RestClient consent(MtlsClientFactory clientFactory, ConsentProps consentProps) {
		return clientFactory.createMtlsClient("consent", consentProps.baseUrl());
	}

	@Bean("educationline")
	RestClient educationline(MtlsClientFactory clientFactory, EducationLineProps educationLineProps) {
		return clientFactory.createMtlsClient("educationline", educationLineProps.baseUrl());
	}

	@Bean("instructor")
	RestClient instructor(MtlsClientFactory clientFactory, InstructorProps instructorProps) {
		return clientFactory.createMtlsClient("instructor", instructorProps.baseUrl());
	}

	@Bean("login")
	RestClient login(MtlsClientFactory clientFactory, LoginProps loginProps) {
		return clientFactory.createMtlsClient("login", loginProps.baseUrl());
	}

	@Bean("organization")
	RestClient organization(MtlsClientFactory clientFactory, OrganizationProps loginProps) {
		return clientFactory.createMtlsClient("organization", loginProps.baseUrl());
	}

	@Bean("person")
	RestClient person(MtlsClientFactory clientFactory, PersonProps personProps) {
		return clientFactory.createMtlsClient("person", personProps.baseUrl());
	}
}
