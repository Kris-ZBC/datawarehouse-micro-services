package local.sop.datawarehouse.gateway.admin.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
 
import local.sop.common.libs.infrastructure.security.context.GatewaySessionContextFilter;
 
@Configuration
public class SessionContextFilterConfig {
 
    @Bean
    FilterRegistrationBean<GatewaySessionContextFilter> gatewaySessionContextFilter(
            @Qualifier("login") RestClient loginClient, SessionCookieProps cookieProps) {
 
        GatewaySessionContextFilter filter = new GatewaySessionContextFilter(
                loginClient, cookieProps.name(), cookieProps.cacheTtl());
 
        FilterRegistrationBean<GatewaySessionContextFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/api/*");
        return registration;
    }
}
