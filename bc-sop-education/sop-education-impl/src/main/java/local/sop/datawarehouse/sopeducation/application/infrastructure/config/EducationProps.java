package local.sop.datawarehouse.sopeducation.application.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "education")
public record EducationProps(String baseUrl) {

}
