package local.sop.datawarehouse.gateway.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// gw-admin never logs anyone in/out (that's gw-common's job — this
// module has no LoginController and never will). This client exists
// purely so GatewaySessionContextFilter can call bc-login's
// /internal/logins/sessions/validate to resolve the caller's role from
// the SOP_SESSION cookie on requests that land on gw-admin's own edge.
// Same base-url property name gw-common uses ("login.base-url"), so
// both gateways point at bc-login through identical config.
@ConfigurationProperties(prefix = "login")
public record SessionValidationProps(String baseUrl) {}
