package local.sop.datawarehouse.anonymize.saga.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "person")
public record PersonProps(String baseUrl) {
}
