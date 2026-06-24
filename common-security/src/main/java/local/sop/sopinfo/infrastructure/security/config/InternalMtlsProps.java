package local.sop.sopinfo.infrastructure.security.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="internal.mtls")
public record InternalMtlsProps(List<String> allowedCallers) {
     public List<String> allowedCallers() {
        return allowedCallers != null ? allowedCallers : List.of();
    }
}
