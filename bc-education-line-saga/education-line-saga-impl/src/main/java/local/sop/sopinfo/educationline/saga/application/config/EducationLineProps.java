package local.sop.sopinfo.educationline.saga.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "education-line")
public record EducationLineProps(String baseUrl) {}
