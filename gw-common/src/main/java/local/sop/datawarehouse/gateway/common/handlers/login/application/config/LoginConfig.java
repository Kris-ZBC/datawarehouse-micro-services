package local.sop.datawarehouse.gateway.common.handlers.login.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
 
import local.sop.common.libs.infrastructure.security.config.MtlsClientFactory;
 
@Configuration
public class LoginConfig {
 
    @Bean("login-saga")
    RestClient loginSagaClient(MtlsClientFactory mtlsClientFactory, LoginSagaProps props) {
        return mtlsClientFactory.createMtlsClient("login-saga", props.baseUrl());
    }
 
    @Bean("login")
    RestClient loginClient(MtlsClientFactory mtlsClientFactory, LoginProps props) {
        return mtlsClientFactory.createMtlsClient("login", props.baseUrl());
    }
}
