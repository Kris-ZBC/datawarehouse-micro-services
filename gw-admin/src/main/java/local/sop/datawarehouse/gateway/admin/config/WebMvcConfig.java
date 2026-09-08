package local.sop.datawarehouse.gateway.admin.config;

import java.util.Set;
 
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
 
import local.sop.common.libs.infrastructure.security.context.RoleAuthorizationInterceptor;
import local.sop.common.libs.sharedkernel.enums.UserRole;
 
// Fail-closed floor, not the primary gate — the primary gate is
// @AllowRoles on each controller (e.g. UserHandlerController's
// class-level annotation). Per RoleAuthorizationInterceptor, an
// @AllowRoles present anywhere on the handler (method or class)
// bypasses this set entirely — "regardless of the default policy" —
// so this costs annotated controllers nothing.
//
// APPRENTICE specifically: this is the one role that must never reach
// gw-admin under any circumstances, annotated or not (per the original
// handoff requirement). Listing it here means a future admin method
// that forgets its @AllowRoles still blocks APPRENTICE by default.
//
// STILL NOT a true "deny unless explicitly allowed" gate: this is a
// deny-list, so a forgotten @AllowRoles on a future method is only
// safe against roles named here. A new UserRole value added later
// would still fall through to allow unless someone remembers to add it
// here too — closing that fully needs RoleAuthorizationInterceptor to
// support an allow-list mode instead of/alongside its current deny-list.
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
 
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RoleAuthorizationInterceptor(Set.of(UserRole.APPRENTICE)))
                .addPathPatterns("/api/**");
    }
}
 
