package local.sop.datawarehouse.message.saga.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "educationline")
public record EducationLineProps(String baseUrl) {

}
