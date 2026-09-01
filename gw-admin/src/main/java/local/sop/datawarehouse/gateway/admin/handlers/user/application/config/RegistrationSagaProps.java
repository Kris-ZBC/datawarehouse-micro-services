package local.sop.datawarehouse.gateway.admin.handlers.user.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "registration-saga")
public record RegistrationSagaProps(String baseUrl) {}
