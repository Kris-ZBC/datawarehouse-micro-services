package local.sop.sopinfo.educationline.saga.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auditlog")
public record AuditlogProps(String baseUrl) { }
