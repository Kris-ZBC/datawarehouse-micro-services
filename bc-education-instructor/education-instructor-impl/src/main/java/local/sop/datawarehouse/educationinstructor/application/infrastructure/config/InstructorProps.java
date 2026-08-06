package local.sop.datawarehouse.educationinstructor.application.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services.instructor")
public record InstructorProps(String baseUrl) { }
