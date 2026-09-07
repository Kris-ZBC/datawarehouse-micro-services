package local.sop.datawarehouse.gateway.common.handlers.login.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
 
// Direct-to-bc-login client — logout is trivial/single-BC, bypasses
// the saga 
// writes/multi-BC reads go through a saga, single-BC reads/actions
// don't need one).
@ConfigurationProperties(prefix = "login")
public record LoginProps(String baseUrl) {}
