package local.sop.sopinfo.educationline.saga.application.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import local.sop.sopinfo.infrastructure.security.config.MtlsClientFactory;

@Configuration
@EnableConfigurationProperties({EducationLineProps.class, EducationProps.class, AuditlogProps.class})
public class ServiceClientConfig {

	@Bean("education-line")
	RestClient educationLineClient(MtlsClientFactory clientFactory, EducationLineProps props) {
		return clientFactory.createMtlsClient("education-line", props.baseUrl());
	}
	
	@Bean("education")
	RestClient educationClient(MtlsClientFactory clientFactory, EducationProps props) {
		return clientFactory.createMtlsClient("education", props.baseUrl());
	}
	
	@Bean("auditlog")
	RestClient auditlogClient(MtlsClientFactory clientFactory, AuditlogProps props) {
		return clientFactory.createMtlsClient("auditlog", props.baseUrl());
	}
}
