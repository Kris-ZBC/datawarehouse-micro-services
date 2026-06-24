package local.sop.sopinfo.infrastructure.core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import local.sop.sopinfo.infrastructure.core.logging.CoreLoggingProperties;
import org.springframework.test.annotation.DirtiesContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CoreLoggingAutoConfigurationTest {

    private ApplicationContextRunner contextRunner;
    private LoggerContext loggerContext;
    private Logger rootLogger;

    @BeforeEach
    void setUp() {
        contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CoreAutoConfiguration.class))
            .withPropertyValues(
                "sopinfo.core.logging.enabled=true",
                "sopinfo.core.logging.console.error-only=true",
                "sopinfo.core.logging.file.enabled=true"
            );
        
        loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
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
    void shouldAutoConfigureLoggingProperties() {
        contextRunner.run(context -> {
            // Tjek med korrekt bean navn
            String beanName = "sopinfo.core.logging-local.sop.sopinfo.infrastructure.core.logging.CoreLoggingProperties";
            assertTrue(context.containsBean(beanName));
            
            // Hent properties
            CoreLoggingProperties properties = context.getBean(CoreLoggingProperties.class);
            
            // Verificer værdier
            assertTrue(properties.isEnabled());
            assertTrue(properties.getConsole().isErrorOnly());
            assertTrue(properties.getFile().isEnabled());
        });
    }

    @Test
    void shouldConfigureConsoleErrorOnlyAppender() {
        contextRunner.run(context -> {
            ConsoleAppender<?> consoleAppender = (ConsoleAppender<?>) rootLogger.getAppender("SOPINFO-ERROR-CONSOLE");
            assertNotNull(consoleAppender);
            assertTrue(consoleAppender.isStarted());
        });
    }

    @Test
    void shouldConfigureFileAppenders() {
        contextRunner.run(context -> {
            RollingFileAppender<?> appLogAppender = (RollingFileAppender<?>) rootLogger.getAppender("SOPINFO-APP-LOG");
            RollingFileAppender<?> errorLogAppender = (RollingFileAppender<?>) rootLogger.getAppender("SOPINFO-ERROR-LOG");

            assertNotNull(appLogAppender);
            assertNotNull(errorLogAppender);
            assertTrue(appLogAppender.isStarted());
            assertTrue(errorLogAppender.isStarted());
        });
    }

    @Test
    void shouldSetCorrectLogLevels() {
        contextRunner.run(context -> {
            assertEquals(Level.INFO, rootLogger.getLevel());
            
            Logger sopinfoLogger = loggerContext.getLogger("local.sop.sopinfo");
            assertEquals(Level.DEBUG, sopinfoLogger.getLevel());
            
            Logger springLogger = loggerContext.getLogger("org.springframework");
            assertEquals(Level.WARN, springLogger.getLevel());
        });
    }

    @Test
    void shouldNotConfigureWhenDisabled() {
        ApplicationContextRunner disabledRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CoreAutoConfiguration.class))
            .withPropertyValues("sopinfo.core.logging.enabled=false");

        disabledRunner.run(context -> {
            assertNull(rootLogger.getAppender("SOPINFO-ERROR-CONSOLE"));
            assertNull(rootLogger.getAppender("SOPINFO-APP-LOG"));
        });
    }
}