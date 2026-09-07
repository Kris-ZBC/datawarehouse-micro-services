package local.sop.datawarehouse.gateway.common.handlers.login.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
 
@ConfigurationProperties(prefix = "login-saga")
public record LoginSagaProps(String baseUrl) {}
 
