package local.sop.sopinfo.infrastructure.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import local.sop.sopinfo.infrastructure.core.logging.CoreLoggingProperties;

import static org.junit.jupiter.api.Assertions.*;
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CoreLoggingPropertiesTest {

    private CoreLoggingProperties properties;

    @BeforeEach
    void setUp() {
        properties = new CoreLoggingProperties();
    }

    @Test
    void shouldHaveDefaultValues() {
        // Then
        assertTrue(properties.isEnabled());
        assertFalse(properties.isDebugMode());
        assertTrue(properties.getConsole().isErrorOnly());
        assertTrue(properties.getConsole().isColorEnabled());
        assertTrue(properties.getFile().isEnabled());
        assertTrue(properties.getFile().isUseServiceName());
        assertEquals("INFO", properties.getLevel().getRoot());
        assertEquals("DEBUG", properties.getLevel().getApplication());
        assertEquals("WARN", properties.getLevel().getExternal());
    }

    @Test
    void shouldSetConsoleProperties() {
        // When
        properties.getConsole().setErrorOnly(false);
        properties.getConsole().setPattern("%d{HH:mm:ss} %msg%n");
        properties.getConsole().setColorEnabled(false);

        // Then
        assertFalse(properties.getConsole().isErrorOnly());
        assertEquals("%d{HH:mm:ss} %msg%n", properties.getConsole().getPattern());
        assertFalse(properties.getConsole().isColorEnabled());
    }

    @Test
    void shouldSetFileProperties() {
        // When
        properties.getFile().setBasePath("/var/log");
        properties.getFile().setApplicationLog("app.log");
        properties.getFile().setErrorLog("error.log");
        properties.getFile().setMaxFileSize("50MB");
        properties.getFile().setMaxHistory(7);
        properties.getFile().setUseServiceName(false);

        // Then
        assertEquals("/var/log", properties.getFile().getBasePath());
        assertEquals("app.log", properties.getFile().getApplicationLog());
        assertEquals("error.log", properties.getFile().getErrorLog());
        assertEquals("50MB", properties.getFile().getMaxFileSize());
        assertEquals(7, properties.getFile().getMaxHistory());
        assertFalse(properties.getFile().isUseServiceName());
    }

    @Test
    void shouldSetLevelProperties() {
        // When
        properties.getLevel().setRoot("WARN");
        properties.getLevel().setApplication("INFO");
        properties.getLevel().setExternal("ERROR");
        properties.getLevel().setFramework("DEBUG");

        // Then
        assertEquals("WARN", properties.getLevel().getRoot());
        assertEquals("INFO", properties.getLevel().getApplication());
        assertEquals("ERROR", properties.getLevel().getExternal());
        assertEquals("DEBUG", properties.getLevel().getFramework());
    }

    @Test
    void shouldSetMainEnabledProperty() {
        // When
        properties.setEnabled(false);
        properties.setDebugMode(true);

        // Then
        assertFalse(properties.isEnabled());
        assertTrue(properties.isDebugMode());
    }

    @Test
    void shouldHandleDefaultConsolePattern() {
        // When
        String pattern = properties.getConsole().getPattern();

        // Then
        assertNotNull(pattern);
        assertFalse(pattern.isEmpty());
        assertTrue(pattern.contains("%d"));
        assertTrue(pattern.contains("%-5level"));
    }

    @Test
    void shouldHandleDefaultFilePattern() {
        // When
        String pattern = properties.getFile().getPattern();

        // Then
        assertNotNull(pattern);
        assertFalse(pattern.isEmpty());
        assertTrue(pattern.contains("%d"));
        assertTrue(pattern.contains("%-5level"));
    }

    @Test
    void shouldHaveValidDefaultFileNames() {
        // When
        String appLog = properties.getFile().getApplicationLog();
        String errorLog = properties.getFile().getErrorLog();

        // Then
        assertEquals("application.log", appLog);
        assertEquals("error.log", errorLog);
    }

}
