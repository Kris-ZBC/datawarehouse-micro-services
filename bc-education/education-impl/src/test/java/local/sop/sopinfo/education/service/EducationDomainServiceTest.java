package local.sop.sopinfo.education.service;

import local.sop.sopinfo.education.domain.model.Education;
import local.sop.sopinfo.education.domain.model.valueobjects.EducationCategory;
import local.sop.sopinfo.education.domain.model.valueobjects.EducationName;
import local.sop.sopinfo.education.domain.service.EducationDomainService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EducationDomainServiceTest {

    private EducationDomainService educationDomainService;

    @BeforeEach
    void setUp() {
        educationDomainService = new EducationDomainService();
    }

    @Test
    void shouldCreateEducationCorrectly() {
        EducationName name = new EducationName("Software Developer");
        EducationCategory category = new EducationCategory("Technical");
        
        Education input = Education.builder()
                .name(name)
                .category(category)
                .active(true)
                .build();

        Education result = educationDomainService.createEducation(input);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getCategory()).isEqualTo(category);
        assertThat(result.isActive()).isFalse();
    }
}