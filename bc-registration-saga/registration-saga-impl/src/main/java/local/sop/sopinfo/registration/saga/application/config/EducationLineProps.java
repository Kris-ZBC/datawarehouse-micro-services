package local.sop.sopinfo.registration.saga.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "educationline")
public record EducationLineProps (String baseUrl) {
    
}
