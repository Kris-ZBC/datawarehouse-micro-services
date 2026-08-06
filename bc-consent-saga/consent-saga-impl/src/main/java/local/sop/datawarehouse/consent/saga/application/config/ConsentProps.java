package local.sop.datawarehouse.consent.saga.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "consent")
public record ConsentProps(String baseUrl) {

}
