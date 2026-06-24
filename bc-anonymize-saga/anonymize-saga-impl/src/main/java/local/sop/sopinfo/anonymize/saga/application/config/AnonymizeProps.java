package local.sop.sopinfo.anonymize.saga.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "anonymize")
public record AnonymizeProps(String baseUrl) {
}
