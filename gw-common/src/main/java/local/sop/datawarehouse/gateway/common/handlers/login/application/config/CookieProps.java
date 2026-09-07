package local.sop.datawarehouse.gateway.common.handlers.login.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Domain scoped to the parent domain so every portal under it
// (SopInfo, Quiz, InfoMeet, ...) receives the same cookie automatically.
@ConfigurationProperties(prefix = "sop.session-cookie")
public record CookieProps(String name, String domain) {
    public CookieProps {
        if (name == null || name.isBlank()) name = "SOP_SESSION";
        if (domain == null || domain.isBlank()) domain = ".sop.local";
    }
}
