package local.sop.datawarehouse.message.saga.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auditlog")
public record AuditLogProps(String baseUrl) {

}
