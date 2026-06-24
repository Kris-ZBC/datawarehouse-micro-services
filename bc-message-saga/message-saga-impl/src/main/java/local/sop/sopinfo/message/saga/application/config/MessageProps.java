package local.sop.sopinfo.message.saga.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "message")
public record MessageProps(String baseUrl) {

}
