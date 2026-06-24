package local.sop.sopinfo.auditlog.config;

import local.sop.sopinfo.auditlog.domain.service.AuditLogDomain;
import local.sop.sopinfo.auditlog.domain.service.AuditlogDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditlogDomainConfig {

    @Bean
    public AuditLogDomain auditLogDomain() {
        return new AuditlogDomainService();
    }
}

