package local.sop.sopinfo.messageperson.application.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "message")
public record MessageProps(String baseUrl) {

}
