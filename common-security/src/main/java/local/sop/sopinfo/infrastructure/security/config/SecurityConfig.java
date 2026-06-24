package local.sop.sopinfo.infrastructure.security.config;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import jakarta.annotation.PostConstruct;
import local.sop.sopinfo.sharedkernel.exceptions.NotFoundException;

@AutoConfiguration
@ConditionalOnProperty(name = "security.enabled", havingValue = "true")
@PropertySource("classpath:application-security.properties")
@EnableConfigurationProperties(InternalMtlsProps.class)
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final InternalMtlsProps props;
    
    public SecurityConfig(InternalMtlsProps props) {
        this.props = props;
    }
    
    @PostConstruct
    public void init() {
        log.info("=== SecurityConfig Debug ===");
        log.info("Allowed callers: {}", props.allowedCallers());
        log.info("Props null: {}", props == null);
        log.info("Allowed callers empty: {}", props.allowedCallers().isEmpty());
        log.info("========================");
    }
    
    @Bean
    public UserDetailsService internalX509UserDetailsService() {
        Set<String> allowed = props.allowedCallers() == null ? Set.of() : Set.copyOf(props.allowedCallers());
        
        return username -> {
            if (allowed.contains(username)) {
                return User.withUsername(username)
                    .password("")
                    .authorities("ROLE_INTERNAL")
                    .build();
            } else {
                throw new NotFoundException("CN.username.notfound", 
                    Map.of("field", "username", "value", username));
            }
        };
    }
    
    @Bean
    @Order(0)
    SecurityFilterChain internalApi(HttpSecurity http, UserDetailsService internalX509UserDetailsService) throws Exception {
       // Handle null props
        Set<String> allowed = props != null && props.allowedCallers() != null ? 
            new HashSet<>(props.allowedCallers()) : Set.of();
        
        if (allowed.isEmpty()) {
            log.warn("No allowed callers configured for internal API - all requests will be denied");
        }
       AuthorizationManager<RequestAuthorizationContext> onlyAllowedCallers =
            (authentication, context) -> {
                var a = authentication.get();
                if(a == null || !a.isAuthenticated()) return new AuthorizationDecision(false);
                return new AuthorizationDecision(allowed.contains(a.getName()));
            };
        http
            .securityMatcher("/internal/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().access(onlyAllowedCallers))
            .x509(x -> x.subjectPrincipalRegex("CN=(.*?)(?:,|$)"))
            .userDetailsService(internalX509UserDetailsService);
        return http.build();
    }

    @Bean
    @Order(1)
    SecurityFilterChain actuator(HttpSecurity http) throws Exception {
        http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
                .anyRequest().denyAll()
            );

        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain publicApi(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/**")
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().denyAll());

        return http.build();
    }



}
