package local.sop.datawarehouse.gateway.common.handlers.organisation.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "organisation")
public record OrganisationProps(String baseUrl) {

}
