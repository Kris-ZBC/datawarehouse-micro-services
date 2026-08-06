package local.sop.datawarehouse.registration.saga.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "apprentice")
public record ApprenticeProps(String baseUrl) {

}
