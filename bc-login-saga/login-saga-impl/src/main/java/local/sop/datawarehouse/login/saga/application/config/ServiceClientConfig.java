package local.sop.datawarehouse.login.saga.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.infrastructure.security.config.MtlsClientFactory;

@Configuration
public class ServiceClientConfig {

	@Bean("login")
	RestClient login(MtlsClientFactory clientFactory, LoginProps loginProps) {
		return clientFactory.createMtlsClient("login", loginProps.baseUrl());
	}

	@Bean("consent")
	RestClient consent(MtlsClientFactory clientFactory, ConsentProps consentProps) {
		return clientFactory.createMtlsClient("consent", consentProps.baseUrl());
	}

	@Bean("auditLog")
	RestClient auditlog(MtlsClientFactory clientFactory, AuditlogProps auditLogProps) {
		return clientFactory.createMtlsClient("auditlog", auditLogProps.baseurl());
	}

	// NEW: role resolution — login-saga calls these to determine
	// whether a freshly-authenticated person is an instructor or
	// apprentice, before creating their session.
	@Bean("instructor")
	RestClient instructor(MtlsClientFactory clientFactory, InstructorProps instructorProps) {
		return clientFactory.createMtlsClient("instructor", instructorProps.baseUrl());
	}

	@Bean("apprentice")
	RestClient apprentice(MtlsClientFactory clientFactory, ApprenticeProps apprenticeProps) {
		return clientFactory.createMtlsClient("apprentice", apprenticeProps.baseUrl());
	}
}