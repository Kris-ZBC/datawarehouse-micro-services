package local.sop.datawarehouse.gateway.admin.config;

import java.time.Duration;
 
import org.springframework.boot.context.properties.ConfigurationProperties;

// Same cookie gw-common's LoginController sets (name/domain must match
// exactly — the cookie is scoped to .sop.local so every gateway on the
// domain receives it automatically). gw-admin only ever *reads* this
// cookie (via GatewaySessionContextFilter) to resolve the caller's role;
// it never issues or clears it — that stays gw-common's job (login/logout
// are single-BC, no reason to duplicate the flow here).
@ConfigurationProperties(prefix = "sop.session-cookie")
public record SessionCookieProps(String name, long cacheTtlSeconds) {
    public SessionCookieProps {
        if (name == null || name.isBlank()) name = "SOP_SESSION";
        if (cacheTtlSeconds <= 0) cacheTtlSeconds = 30;
    }
 
    public Duration cacheTtl() {
        return Duration.ofSeconds(cacheTtlSeconds);
    }
}
