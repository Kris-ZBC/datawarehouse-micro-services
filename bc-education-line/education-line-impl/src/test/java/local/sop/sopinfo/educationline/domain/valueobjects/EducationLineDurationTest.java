package local.sop.sopinfo.educationline.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineDuration;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public class EducationLineDurationTest {

    @Test
    void happyPath_valid_duration_succeeds() {
        EducationLineDuration duration = new EducationLineDuration(1, 2, 3);
        assertEquals(1, duration.getDurationYears());
        assertEquals(2, duration.getDurationMonths());
        assertEquals(3, duration.getDurationDays());
        assertEquals(14, duration.totalMonths());
        assertEquals(423, duration.totalDays());
        assertTrue(duration.asString().contains("1 year"));
    }

    @Test
    void happyPath_asString_formats_correctly() {
        assertEquals("2 years 3 months 4 days", new EducationLineDuration(2, 3, 4).asString().replace("  ", " "));
        assertEquals("5 years", new EducationLineDuration(5, 0, 0).asString());
        assertEquals("6 months", new EducationLineDuration(0, 6, 0).asString());
        assertEquals("7 days", new EducationLineDuration(0, 0, 7).asString());
    }

    @Test
    void happyPath_asString_singular_plural_and_spacing() {
        assertEquals("1 year", new EducationLineDuration(1, 0, 0).asString());
        assertEquals("2 years", new EducationLineDuration(2, 0, 0).asString());
        assertEquals("1 month", new EducationLineDuration(0, 1, 0).asString());
        assertEquals("2 months", new EducationLineDuration(0, 2, 0).asString());
        assertEquals("1 day", new EducationLineDuration(0, 0, 1).asString());
        assertEquals("2 days", new EducationLineDuration(0, 0, 2).asString());
        assertEquals("1 year 1 month", new EducationLineDuration(1, 1, 0).asString());
        assertEquals("1 year 1 day", new EducationLineDuration(1, 0, 1).asString());
        assertEquals("1 month 1 day", new EducationLineDuration(0, 1, 1).asString());
        assertEquals("1 year 1 month 1 day", new EducationLineDuration(1, 1, 1).asString());
    }

    @Test
    void unhappyPath_negative_years_fails() {
        assertThrows(ValidationException.class, () -> new EducationLineDuration(-1, 0, 0));
    }

    @Test
    void unhappyPath_invalid_months_fails() {
        assertThrows(ValidationException.class, () -> new EducationLineDuration(0, -1, 0));
        assertThrows(ValidationException.class, () -> new EducationLineDuration(0, 12, 0));
    }

    @Test
    void unhappyPath_invalid_days_fails() {
        assertThrows(ValidationException.class, () -> new EducationLineDuration(0, 0, -1));
        assertThrows(ValidationException.class, () -> new EducationLineDuration(0, 0, 31));
    }

    @Test
    void unhappyPath_all_zero_fails() {
        assertThrows(ValidationException.class, () -> new EducationLineDuration(0, 0, 0));
    }
}
