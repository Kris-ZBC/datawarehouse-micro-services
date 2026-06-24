package local.sop.sopinfo.infrastructure.core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import local.sop.sopinfo.infrastructure.core.logging.CoreLoggingConfigurator;
import local.sop.sopinfo.infrastructure.core.logging.CoreLoggingProperties;
import org.springframework.test.annotation.DirtiesContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CoreLoggingConfiguratorTest {
    private LoggerContext loggerContext;
    private Logger rootLogger;
    private CoreLoggingProperties properties;

    @BeforeEach
    void setUp() {
        loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        properties = createDefaultProperties();
    }

    @AfterEach
    void tearDown() {
        rootLogger.iteratorForAppenders().forEachRemaining(appender -> {
            if (appender.getName() != null && appender.getName().startsWith("SOPINFO-")) {
                rootLogger.detachAppender(appender);
            }
        });
    }

    @Test
    void shouldConfigureConsoleErrorOnlyWhenEnabled() {
        // Given
        properties.getConsole().setErrorOnly(true);

        // When
        CoreLoggingConfigurator.configure(loggerContext, properties);

        // Then
        ConsoleAppender<?> consoleAppender = (ConsoleAppender<?>) rootLogger.getAppender("SOPINFO-ERROR-CONSOLE");
        assertNotNull(consoleAppender);
        assertTrue(consoleAppender.isStarted());
    }

    @Test
    void shouldNotConfigureConsoleWhenErrorOnlyIsFalse() {
        // Given
        properties.getConsole().setErrorOnly(false);

        // When
        CoreLoggingConfigurator.configure(loggerContext, properties);

        // Then
        ConsoleAppender<?> consoleAppender = (ConsoleAppender<?>) rootLogger.getAppender("SOPINFO-ERROR-CONSOLE");
        assertNull(consoleAppender);
    }

    @Test
    void shouldConfigureFileAppendersWhenEnabled() {
        // Given
        properties.getFile().setEnabled(true);

        // When
        CoreLoggingConfigurator.configure(loggerContext, properties);

        // Then
        RollingFileAppender<?> appLogAppender = (RollingFileAppender<?>) rootLogger.getAppender("SOPINFO-APP-LOG");
        RollingFileAppender<?> errorLogAppender = (RollingFileAppender<?>) rootLogger.getAppender("SOPINFO-ERROR-LOG");

        assertNotNull(appLogAppender);
        assertNotNull(errorLogAppender);
        assertTrue(appLogAppender.isStarted());
        assertTrue(errorLogAppender.isStarted());
    }

    @Test
    void shouldNotConfigureFileAppendersWhenDisabled() {
        // Given
        properties.getFile().setEnabled(false);

        // When
        CoreLoggingConfigurator.configure(loggerContext, properties);

        // Then
        RollingFileAppender<?> appLogAppender = (RollingFileAppender<?>) rootLogger.getAppender("SOPINFO-APP-LOG");
        assertNull(appLogAppender);
    }

    @Test
    void shouldSetCorrectLogLevels() {
        // Given
        properties.getLevel().setRoot("WARN");
        properties.getLevel().setApplication("INFO");
        properties.getLevel().setExternal("ERROR");

        // When
        CoreLoggingConfigurator.configure(loggerContext, properties);

        // Then
        assertEquals(Level.WARN, rootLogger.getLevel());
        
        Logger sopinfoLogger = loggerContext.getLogger("local.sop.sopinfo");
        assertEquals(Level.INFO, sopinfoLogger.getLevel());
        
        Logger springLogger = loggerContext.getLogger("org.springframework");
        assertEquals(Level.ERROR, springLogger.getLevel());
    }

    @Test
    void shouldNotConfigureWhenDisabled() {
        // Given
        properties.setEnabled(false);

        // When
        CoreLoggingConfigurator.configure(loggerContext, properties);

        // Then
        assertNull(rootLogger.getAppender("SOPINFO-ERROR-CONSOLE"));
        assertNull(rootLogger.getAppender("SOPINFO-APP-LOG"));
        assertNull(rootLogger.getAppender("SOPINFO-ERROR-LOG"));
    }

    @Test
    void shouldUseServiceNameInFilePath() {
        // Given
        System.setProperty("spring.application.name", "test-service");
        properties.getFile().setUseServiceName(true);

        // When
        CoreLoggingConfigurator.configure(loggerContext, properties);

        // Then
        RollingFileAppender<?> appLogAppender = (RollingFileAppender<?>) rootLogger.getAppender("SOPINFO-APP-LOG");
        assertNotNull(appLogAppender);
        assertTrue(appLogAppender.getFile().contains("test-service"));
        
        // Cleanup
        System.clearProperty("spring.application.name");
    }

    @Test
    void shouldUseDefaultPathWhenNoServiceName() {
        // Given
        System.clearProperty("spring.application.name");
        properties.getFile().setUseServiceName(true);

        // When
        CoreLoggingConfigurator.configure(loggerContext, properties);

        // Then
        RollingFileAppender<?> appLogAppender = (RollingFileAppender<?>) rootLogger.getAppender("SOPINFO-APP-LOG");
        assertNotNull(appLogAppender);
        assertTrue(appLogAppender.getFile().contains("application"));
    }

    private CoreLoggingProperties createDefaultProperties() {
        CoreLoggingProperties props = new CoreLoggingProperties();
        props.setEnabled(true);
        props.getConsole().setErrorOnly(true);
        props.getFile().setEnabled(true);
        props.getLevel().setRoot("INFO");
        props.getLevel().setApplication("DEBUG");
        props.getLevel().setExternal("WARN");
        return props;
    }
}
