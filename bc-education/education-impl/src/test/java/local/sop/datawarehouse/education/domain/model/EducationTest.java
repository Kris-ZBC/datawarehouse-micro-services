package local.sop.datawarehouse.education.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.education.domain.model.valueobjects.EducationCategory;
import local.sop.datawarehouse.education.domain.model.valueobjects.EducationId;
import local.sop.datawarehouse.education.domain.model.valueobjects.EducationName;

class EducationTest {

    private final EducationId id = EducationId.newId();
    private final EducationName name = new EducationName("Software Developer");
    private final EducationCategory category = new EducationCategory("Technical");

    @Test
    void shouldCreateEducationWithValidValues() {
        Education education = new Education(id, name, category, false);
        assertThat(education.getId()).isEqualTo(id);
        assertThat(education.getName()).isEqualTo(name);
        assertThat(education.getCategory()).isEqualTo(category);
        assertThat(education.isActive()).isFalse();
    }

    @Test
    void shouldThrowWhenIdIsNull() {
        assertThatThrownBy(() -> new Education(null, name, category, false))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowWhenNameIsNull() {
        assertThatThrownBy(() -> new Education(id, null, category, false))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowWhenCategoryIsNull() {
        assertThatThrownBy(() -> new Education(id, name, null, false))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowWhenActiveIsNull() {
        assertThatThrownBy(() -> new Education(id, name, category, null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldGenerateIdWhenNotProvided() {
        Education education = Education.create(null, name, category, false);
        assertThat(education.getId()).isNotNull();
    }

    @Test
    void shouldDefaultActiveToFalseWhenNotProvided() {
        Education education = Education.create(null, name, category, null);
        assertThat(education.isActive()).isFalse();
    }

    @Test
    void shouldCreateEducationUsingBuilder() {
        Education education = Education.builder()
                .id(id)
                .name(name)
                .category(category)
                .active(false)
                .build();
        assertThat(education.getId()).isEqualTo(id);
        assertThat(education.getName()).isEqualTo(name);
        assertThat(education.getCategory()).isEqualTo(category);
        assertThat(education.isActive()).isFalse();
    }

    @Test
    void shouldCreateActiveEducation() {
        Education education = Education.builder()
                .name(name)
                .category(category)
                .active(true)
                .build();
        assertThat(education.isActive()).isTrue();
    }

    @Test
    void shouldGenerateUniqueIdsForDifferentEducations() {
        Education education1 = Education.create(null, name, category, false);
        Education education2 = Education.create(null, name, category, false);
        assertThat(education1.getId()).isNotEqualTo(education2.getId());
    }
}