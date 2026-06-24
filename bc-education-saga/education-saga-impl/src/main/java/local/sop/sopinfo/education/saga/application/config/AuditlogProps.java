package local.sop.sopinfo.education.saga.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auditlog")
public record AuditlogProps(String baseUrl) { }