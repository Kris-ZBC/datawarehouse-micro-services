package local.sop.datawarehouse.education.saga.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "education-instructor")
public record EducationInstructorProps(String baseUrl) {}