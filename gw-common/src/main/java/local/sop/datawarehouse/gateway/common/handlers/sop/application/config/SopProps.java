package local.sop.datawarehouse.gateway.common.handlers.sop.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sop")
public record SopProps(String baseUrl) {

}
