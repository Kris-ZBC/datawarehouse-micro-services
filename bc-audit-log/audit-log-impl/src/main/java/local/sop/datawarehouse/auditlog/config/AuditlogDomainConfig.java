package local.sop.datawarehouse.auditlog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import local.sop.datawarehouse.auditlog.domain.service.AuditLogDomain;
import local.sop.datawarehouse.auditlog.domain.service.AuditlogDomainService;

@Configuration
public class AuditlogDomainConfig {

    @Bean
    public AuditLogDomain auditLogDomain() {
        return new AuditlogDomainService();
    }
}

