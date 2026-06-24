package local.sop.sopinfo.infrastructure.core.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.LevelFilter;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.spi.FilterReply;

public class CoreLoggingConfigurator {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CoreLoggingConfigurator.class);
    public static void configure(LoggerContext context, CoreLoggingProperties properties) {
        // TJEK OM LOGGING ER ENABLED
        if (!properties.isEnabled()) {
            if (properties.isDebugMode()) {
                log.info("🚫 SOPINFO Core Logging disabled - skipping configuration");
            }
            return; // FORLAD TIDLIGT - GØR INGENTING
        }

        configureConsoleAppender(context, properties);
        configureFileAppenders(context, properties);
        setLogLevels(context, properties);
        
        if (properties.isDebugMode()) {
            log.info("✅ SOPINFO Core Logging configured successfully");
        }
    }

    private static void configureConsoleAppender(LoggerContext context, CoreLoggingProperties properties) {
        if (!properties.getConsole().isErrorOnly()) {
            return;
        }

        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        
        // Remove existing console appenders
        rootLogger.iteratorForAppenders().forEachRemaining(appender -> {
            if (appender instanceof ConsoleAppender) {
                rootLogger.detachAppender(appender);
            }
        });

        // Create new console appender
        ConsoleAppender<ch.qos.logback.classic.spi.ILoggingEvent> consoleAppender = 
            new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setName("SOPINFO-ERROR-CONSOLE");

        // Add ERROR level filter
        LevelFilter levelFilter = new LevelFilter();
        levelFilter.setLevel(Level.ERROR);
        levelFilter.setOnMatch(FilterReply.ACCEPT);
        levelFilter.setOnMismatch(FilterReply.DENY);
        levelFilter.start();
        consoleAppender.addFilter(levelFilter);

        // Set encoder
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(properties.getConsole().getPattern());
        encoder.start();
        consoleAppender.setEncoder(encoder);

        consoleAppender.start();
        rootLogger.addAppender(consoleAppender);
    }

    private static void configureFileAppenders(LoggerContext context, CoreLoggingProperties properties) {
        if (!properties.getFile().isEnabled()) {
            return;
        }

        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);

        String applicationLogPath = getLogFilePath(properties, properties.getFile().getApplicationLog());
        String errorLogPath = getLogFilePath(properties, properties.getFile().getErrorLog());

        // Application log file
        RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent> appLogAppender = 
            createRollingFileAppender(context, "SOPINFO-APP-LOG", applicationLogPath, 
                properties.getFile().getPattern(), properties.getFile().getMaxFileSize(), 
                properties.getFile().getMaxHistory());
        rootLogger.addAppender(appLogAppender);

        // Error log file
        RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent> errorLogAppender = 
            createRollingFileAppender(context, "SOPINFO-ERROR-LOG", errorLogPath, 
                properties.getFile().getPattern(), properties.getFile().getMaxFileSize(), 
                properties.getFile().getMaxHistory());

        // Add ERROR filter to error log
        LevelFilter errorFilter = new LevelFilter();
        errorFilter.setLevel(Level.ERROR);
        errorFilter.setOnMatch(FilterReply.ACCEPT);
        errorFilter.setOnMismatch(FilterReply.DENY);
        errorFilter.start();
        errorLogAppender.addFilter(errorFilter);

        rootLogger.addAppender(errorLogAppender);
    }

    private static void setLogLevels(LoggerContext context, CoreLoggingProperties properties) {
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.toLevel(properties.getLevel().getRoot()));

        context.getLogger("local.sop.sopinfo").setLevel(Level.toLevel(properties.getLevel().getApplication()));
        context.getLogger("org.springframework").setLevel(Level.toLevel(properties.getLevel().getExternal()));
        context.getLogger("org.hibernate").setLevel(Level.toLevel(properties.getLevel().getExternal()));
        context.getLogger("com.zaxxer.hikari").setLevel(Level.toLevel(properties.getLevel().getExternal()));
    }

    private static String getLogFilePath(CoreLoggingProperties properties, String fileName) {
        String basePath = properties.getFile().getBasePath();
        
        if (properties.getFile().isUseServiceName()) {
            String serviceName = System.getProperty("spring.application.name", "application");
            basePath = basePath + "/" + serviceName;
        }
        
        return basePath + "/" + fileName;
    }

        private static RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent> createRollingFileAppender(
            LoggerContext context,
            String name,
            String filePath,
            String pattern,
            String maxFileSize,
            int maxHistory) {

        RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender = 
            new RollingFileAppender<>();
        appender.setContext(context);
        appender.setName(name);
        appender.setFile(filePath); // Simpel løsning

        SizeAndTimeBasedRollingPolicy<ch.qos.logback.classic.spi.ILoggingEvent> rollingPolicy = 
            new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(appender);
        rollingPolicy.setFileNamePattern(filePath.replace(".log", ".%d{yyyy-MM-dd}.%i.log.gz"));
        
        ch.qos.logback.core.util.FileSize fileSize = ch.qos.logback.core.util.FileSize.valueOf(maxFileSize);
        rollingPolicy.setMaxFileSize(fileSize);
        
        rollingPolicy.setMaxHistory(maxHistory);
        rollingPolicy.start();
        appender.setRollingPolicy(rollingPolicy);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(pattern);
        encoder.start();
        appender.setEncoder(encoder);

        appender.start();
        return appender;
    }
}