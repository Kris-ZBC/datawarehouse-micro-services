package local.sop.sopinfo.sopinstructor.application.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "instructor")
public record InstructorProps(String baseUrl) {

}
