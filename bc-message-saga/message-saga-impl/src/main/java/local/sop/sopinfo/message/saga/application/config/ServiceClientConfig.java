package local.sop.sopinfo.message.saga.application.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.infrastructure.security.config.MtlsClientFactory;

@Configuration
@EnableConfigurationProperties(MessageProps.class)
public class ServiceClientConfig {

	@Bean("apprentice")
	RestClient apprentice(MtlsClientFactory clientFactory, ApprenticeProps apprenticeProps) {
		return clientFactory.createMtlsClient("apprentice", apprenticeProps.baseUrl());
	}

	@Bean("auditlog")
	RestClient auditLog(MtlsClientFactory clientFactory, AuditLogProps auditLogProps) {
		return clientFactory.createMtlsClient("auditlog", auditLogProps.baseUrl());
	}

	@Bean("educationinstructor")
	RestClient educationInstructor(MtlsClientFactory clientFactory, EducationInstructorProps educationInstructorProps) {
		return clientFactory.createMtlsClient("educationinstructor", educationInstructorProps.baseUrl());
	}

	@Bean("educationline")
	RestClient educationline(MtlsClientFactory clientFactory, EducationLineProps educationlineProps) {
		return clientFactory.createMtlsClient("educationline", educationlineProps.baseUrl());
	}

	@Bean("instructor")
	RestClient instructor(MtlsClientFactory clientFactory, InstructorProps instructorProps) {
		return clientFactory.createMtlsClient("instructor", instructorProps.baseUrl());
	}

	@Bean("messageperson")
	RestClient messagePerson(MtlsClientFactory clientFactory, MessagePersonProps messagePersonProps) {
		return clientFactory.createMtlsClient("messageperson", messagePersonProps.baseUrl());
	}

	@Bean("message")
	RestClient message(MtlsClientFactory clientFactory, MessageProps messageProps) {
		return clientFactory.createMtlsClient("message", messageProps.baseUrl());
	}

	@Bean("notification")
	RestClient notification(MtlsClientFactory clientFactory, NotificationProps notificationProps) {
		return clientFactory.createMtlsClient("notification", notificationProps.baseUrl());
	}

	@Bean("personnotification")
	RestClient personNotification(MtlsClientFactory clientFactory, PersonNotificationProps personNotificationProps) {
		return clientFactory.createMtlsClient("personnotification", personNotificationProps.baseUrl());
	}

	@Bean("person")
	RestClient person(MtlsClientFactory clientFactory, PersonProps personProps) {
		return clientFactory.createMtlsClient("person", personProps.baseUrl());
	}
}
