package local.sop.sopinfo.education.domain.model.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

class EducationCategoryTest {

    @Test
    void shouldCreateEducationCategoryWithValidValue() {
        EducationCategory category = new EducationCategory("Data and Communication");
        assertThat(category.value()).isEqualTo("Data and Communication");
    }

    @Test
    void shouldThrowWhenValueIsNull() {
        assertThatThrownBy(() -> new EducationCategory(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("education.category.required");
    }

    @Test
    void shouldThrowWhenValueIsBlank() {
        assertThatThrownBy(() -> new EducationCategory("   "))
                .isInstanceOf(ValidationException.class)
                .hasMessage("education.category.required");
    }

    @Test
    void shouldTrimWhitespace() {
        EducationCategory category = new EducationCategory("  Data and Communication  ");
        assertThat(category.value()).isEqualTo("Data and Communication");
    }

    @Test
    void shouldThrowWhenValueExceedsMaxLength() {
        String tooLong = "a".repeat(101);
        assertThatThrownBy(() -> new EducationCategory(tooLong))
                .isInstanceOf(ValidationException.class)
                .hasMessage("education.category.length.invalid");
    }

    @Test
    void shouldAcceptValueAtMaxLength() {
        String maxLength = "a".repeat(100);
        EducationCategory category = new EducationCategory(maxLength);
        assertThat(category.value()).isEqualTo(maxLength);
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        EducationCategory category1 = new EducationCategory("Data and Communication");
        EducationCategory category2 = new EducationCategory("Data and Communication");
        assertThat(category1).isEqualTo(category2);
    }
}