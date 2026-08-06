package local.sop.datawarehouse.sopinstructor.application.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "instructor")
public record InstructorProps(String baseUrl) {

}
