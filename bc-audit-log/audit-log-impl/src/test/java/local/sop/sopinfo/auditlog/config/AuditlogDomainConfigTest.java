package local.sop.sopinfo.auditlog.config;

import local.sop.sopinfo.auditlog.domain.service.AuditLogDomain;
import local.sop.sopinfo.auditlog.domain.service.AuditlogDomainService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AuditlogDomainConfig.class)
class AuditlogDomainConfigTest {

    @Autowired
    private AuditLogDomain bean;

    @Test
    void auditLogDomainBean_shouldBePresent() {
        assertNotNull(bean);
        assertTrue(bean instanceof AuditlogDomainService);
    }
}