package local.sop.sopinfo.sharedkernel.sagas.compensate.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public class SagaOutcomeTest {
    @Test
    void testSagaOutcomeValues() {
        SagaOutcome[] values = SagaOutcome.values();
        assertEquals(5, values.length);
    }

    @ParameterizedTest(name = "parse(\"{0}\") should return {0}")
    @EnumSource(SagaOutcome.class)
    public void testParseValidValues(SagaOutcome outcome) {
        assertEquals(outcome, SagaOutcome.parse(outcome.name()));
    }

     @ParameterizedTest(name = "parse(\"{0}\") should match valueOf(\"{0}\")")
    @EnumSource(SagaOutcome.class)
    public void testParseConsistency(SagaOutcome outcome) {
        assertEquals(SagaOutcome.valueOf(outcome.name()), SagaOutcome.parse(outcome.name()));
    }

    @ParameterizedTest(name = "toString() of {0} should equal its name")
    @EnumSource(SagaOutcome.class)
    public void testToString(SagaOutcome outcome) {
        assertEquals(outcome.name(), outcome.toString());
    }

    @ParameterizedTest(name = "valueOf(\"{0}\") should throw IllegalArgumentException")
    @ValueSource(strings = {"INVALID", "succeeded", "SUCC"})
    public void testValueOfInvalidInput(String input) {
        assertThrows(IllegalArgumentException.class, () -> SagaOutcome.valueOf(input));
    }

    @Test
    void enumContract_valueOf_null_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> SagaOutcome.valueOf(null));
    }

    @Test
    void unhappyPath_parseNull_shouldThrowValidationException() {
        ValidationException ex = assertThrows(ValidationException.class, () -> SagaOutcome.parse(null));
        assertEquals("saga.outcome.invalid", ex.getMessage());
    }
}
