package local.sop.sopinfo.educationline.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineName;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public class EducationLineNameTest {

    @Test
    void happyPath_valid_name_succeeds() {
        EducationLineName name = new EducationLineName("Mathematics");
        assertNotNull(name);
        assertEquals("Mathematics", name.value());
    }

    @Test
    void unhappyPath_null_fails() {
        assertThrows(ValidationException.class, () -> new EducationLineName(null));
    }

    @Test
    void unhappyPath_blank_fails() {
        assertThrows(ValidationException.class, () -> new EducationLineName("   "));
    }

    @Test
    void unhappyPath_empty_fails() {
        assertThrows(ValidationException.class, () -> new EducationLineName(""));
    }

    @Test
    void unhappyPath_too_short_fails() {
        assertThrows(ValidationException.class, () -> new EducationLineName(""));
    }

    @Test
    void unhappyPath_too_long_fails() {
        String longName = "A".repeat(101);
        assertThrows(ValidationException.class, () -> new EducationLineName(longName));
    }

    @Test
    void unhappyPath_contains_digit_fails() {
        assertThrows(ValidationException.class, () -> new EducationLineName("Math3matics"));
        assertThrows(ValidationException.class, () -> new EducationLineName("1Mathematics"));
        assertThrows(ValidationException.class, () -> new EducationLineName("Mathematics2"));
    }

    @Test
    void unhappyPath_contains_digit_exception_key() {
        var ex = assertThrows(ValidationException.class, () -> new EducationLineName("Math3matics"));
        assertEquals("educationline.name.contains.number", ex.getMessage());
    }

    @Test
    void happyPath_trimmed_name_succeeds() {
        EducationLineName name = new EducationLineName("  Physics  ");
        assertEquals("Physics", name.value());
    }
}
