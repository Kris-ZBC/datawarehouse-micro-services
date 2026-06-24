package local.sop.sopinfo.education.saga.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "education")
public record EducationProps(String baseUrl) { }
