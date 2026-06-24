package local.sop.sopinfo.sopeducation.application.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sop")
public record SopProps(String baseUrl) {

}
