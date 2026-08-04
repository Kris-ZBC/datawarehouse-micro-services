package local.sop.sopinfo.education.domain.model.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

class EducationNameTest {

    @Test
    void shouldCreateEducationNameWithValidValue() {
        EducationName name = new EducationName("IT Support");
        assertThat(name.value()).isEqualTo("IT Support");
    }

    @Test
    void shouldThrowWhenValueIsNull() {
        assertThatThrownBy(() -> new EducationName(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("education.name.required");
    }

    @Test
    void shouldThrowWhenValueIsBlank() {
        assertThatThrownBy(() -> new EducationName("   "))
                .isInstanceOf(ValidationException.class)
                .hasMessage("education.name.required");
    }

    @Test
    void shouldTrimWhitespace() {
        EducationName name = new EducationName("  IT Support  ");
        assertThat(name.value()).isEqualTo("IT Support");
    }

    @Test
    void shouldThrowWhenValueExceedsMaxLength() {
        String tooLong = "a".repeat(101);
        assertThatThrownBy(() -> new EducationName(tooLong))
                .isInstanceOf(ValidationException.class)
                .hasMessage("education.name.length.invalid");
    }

    @Test
    void shouldAcceptValueAtMaxLength() {
        String maxLength = "a".repeat(100);
        EducationName name = new EducationName(maxLength);
        assertThat(name.value()).isEqualTo(maxLength);
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        EducationName name1 = new EducationName("IT Support");
        EducationName name2 = new EducationName("IT Support");
        assertThat(name1).isEqualTo(name2);
    }
}