package local.sop.datawarehouse.educationinstructor.application.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services.education")
public record EducationProps(String baseUrl) { }
