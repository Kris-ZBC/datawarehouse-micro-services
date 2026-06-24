package local.sop.sopinfo.education.service;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.education.domain.service.EducationDomain;
import local.sop.sopinfo.education.domain.service.EducationDomainConfig;
import local.sop.sopinfo.education.domain.service.EducationDomainService;

import static org.assertj.core.api.Assertions.assertThat;

class EducationDomainConfigTest {

    private final EducationDomainConfig config = new EducationDomainConfig();

    @Test
    void shouldCreateEducationDomainBean() {
        // When
        EducationDomain domain = config.educationDomain();

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain).isInstanceOf(EducationDomainService.class);
    }
}