package local.sop.datawarehouse.registration.saga.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "instructor")
public record InstructorProps(String baseUrl) {
}
