package local.sop.datawarehouse.registration.saga.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "consent-saga")
public record ConsentSagaProps(String baseUrl) {

}
