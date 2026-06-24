package local.sop.sopinfo.personnotification.application.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "notification")
public record NotificationProps(String baseUrl){
    
}
