package local.sop.sopinfo.educationline.domain.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


class EducationLineDomainConfigTest {

    @Test
    void educationLineDomain_returns_domain_service_bean() {
        EducationLineDomainConfig config = new EducationLineDomainConfig();

        EducationLineDomain bean = config.educationLineDomain();

        assertNotNull(bean);
        assertTrue(bean instanceof EducationLineDomainService);
    }
}
