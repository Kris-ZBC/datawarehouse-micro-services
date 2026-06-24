package local.sop.sopinfo.message.saga.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "educationinstructor")
public record EducationInstructorProps(String baseUrl) {

}
