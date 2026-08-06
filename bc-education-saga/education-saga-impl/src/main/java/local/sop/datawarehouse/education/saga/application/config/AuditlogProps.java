package local.sop.datawarehouse.education.saga.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auditlog")
public record AuditlogProps(String baseUrl) { }