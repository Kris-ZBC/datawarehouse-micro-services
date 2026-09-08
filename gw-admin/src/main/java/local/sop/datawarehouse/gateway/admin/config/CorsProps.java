package local.sop.datawarehouse.gateway.admin.config;

import java.util.List;
 
import org.springframework.boot.context.properties.ConfigurationProperties;
 
// Dev-convenience only now that the real topology is confirmed to be
// nginx/Apache always in front (even in local dev), presenting its own
// mTLS identity on every proxied call. This only matters if local dev
// ever runs `ng serve` directly against the gateway instead of through
// a local nginx too — once that's not a thing, this config is a no-op
// (same-origin requests never hit CORS regardless of what's configured
// here) and can be deleted rather than expanded.
@ConfigurationProperties(prefix = "sop.cors")
public record CorsProps(List<String> allowedOriginPatterns) {
    public CorsProps {
        if (allowedOriginPatterns == null || allowedOriginPatterns.isEmpty()) {
            allowedOriginPatterns = List.of("https://*.sop.local:*");
        }
    }
}
