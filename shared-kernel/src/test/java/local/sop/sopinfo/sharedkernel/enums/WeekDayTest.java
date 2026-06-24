package local.sop.sopinfo.sharedkernel.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

class WeekDayTest {

    // ── Happy path ─────────────────────────────────────────────────────────────

    @ParameterizedTest(name = "parse(\"{0}\") should return {0}")
    @EnumSource(WeekDay.class)
    void happyPath_parseValidDay_shouldReturnCorrectEnum(WeekDay day) {
        assertEquals(day, WeekDay.parse(day.name()));
    }

    @ParameterizedTest(name = "parse(\"{0}\") should match valueOf(\"{0}\")")
    @EnumSource(WeekDay.class)
    void happyPath_parseConsistency_shouldMatchValueOf(WeekDay day) {
        assertEquals(WeekDay.valueOf(day.name()), WeekDay.parse(day.name()));
    }

    // ── Enum contract ──────────────────────────────────────────────────────────

    @Test
    void enumContract_values_shouldContainExactlySevenDays() {
        assertEquals(7, WeekDay.values().length);
    }

    @ParameterizedTest(name = "toString() of {0} should equal its name")
    @EnumSource(WeekDay.class)
    void enumContract_toString_shouldMatchName(WeekDay day) {
        assertEquals(day.name(), day.toString());
    }

    @ParameterizedTest(name = "valueOf(\"{0}\") should round-trip correctly")
    @EnumSource(WeekDay.class)
    void enumContract_valueOf_shouldWorkForAllConstants(WeekDay day) {
        assertEquals(day, WeekDay.valueOf(day.name()));
    }

    @ParameterizedTest(name = "valueOf(\"{0}\") should throw IllegalArgumentException")
    @ValueSource(strings = {"INVALID", "monday", "MON"})
    void enumContract_valueOf_invalidInput_shouldThrowIllegalArgumentException(String input) {
        assertThrows(IllegalArgumentException.class, () -> WeekDay.valueOf(input));
    }

    @Test
    void enumContract_valueOf_null_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> WeekDay.valueOf(null));
    }

    // ── Unhappy path ───────────────────────────────────────────────────────────

    @Test
    void unhappyPath_parseNull_shouldThrowValidationException() {
        ValidationException ex = assertThrows(ValidationException.class, () -> WeekDay.parse(null));
        assertEquals("weekday.invalid", ex.getMessage());
    }

    @ParameterizedTest(name = "parse(\"{0}\") should throw ValidationException")
    @ValueSource(strings = {
        "",
        " ", "   ", "\t", "\n", " \t \n ",           // blank / whitespace
        "monday", "tuesday", "wednesday",              // lowercase
        "thursday", "friday", "saturday", "sunday",
        "Monday", "MonDaY", "FRIdaY", "sUnDaY",       // mixed case
        "MON", "FRIDAYDAY", "WEEKEND", "123",          // garbage
        "MØNDAY", "MONDAY!", "MON@DAY",                // special characters
        "MON DAY", "MON-DAY", "MONDAY1"
    })
    void unhappyPath_parseInvalidInput_shouldThrowValidationException(String input) {
        ValidationException ex = assertThrows(ValidationException.class, () -> WeekDay.parse(input));
        assertEquals("weekday.invalid", ex.getMessage());
    }
}
