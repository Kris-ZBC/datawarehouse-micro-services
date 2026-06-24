package local.sop.sopinfo.infrastructure.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import local.sop.sopinfo.infrastructure.security.config.InternalMtlsProps;

@SpringBootApplication
@EnableConfigurationProperties(InternalMtlsProps.class)
public class SecurityTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecurityTestApplication.class, args);
    }
}
