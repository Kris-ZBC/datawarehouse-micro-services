package local.sop.datawarehouse.personnotification.application.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "person")
public record PersonProps(String baseUrl){
    
}
