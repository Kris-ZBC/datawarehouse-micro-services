package local.sop.datawarehouse.gateway.admin.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.infrastructure.security.config.MtlsClientFactory;

// Builds the read-only "login" RestClient used exclusively by
// GatewaySessionContextFilter for session validation. Not a login
// flow — gw-admin has no LoginController and issues/clears no cookies.
@Configuration
@EnableConfigurationProperties({SessionValidationProps.class, SessionCookieProps.class})
public class SessionValidationClientConfig {
 
    @Bean("login")
    RestClient loginClient(MtlsClientFactory mtlsClientFactory, SessionValidationProps props) {
        return mtlsClientFactory.createMtlsClient("login", props.baseUrl());
    }
}
