package local.sop.sopinfo.message.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import local.sop.sopinfo.message.MessageStartApplication;

class ArchPropsSanityTest {

    @Test
    @DisplayName("should use expected base package")
    void shouldUseExpectedBasePackage() {
        assertEquals("local.sop.sopinfo.message", MessageStartApplication.class.getPackageName());
    }

    @Test
    @DisplayName("should have start application class")
    void shouldHaveStartApplicationClass() {
        assertTrue(MessageStartApplication.class.getSimpleName().contains("Application"));
    }
}