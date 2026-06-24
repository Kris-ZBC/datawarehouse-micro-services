package local.sop.sopinfo.infrastructure.core;

import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import jakarta.annotation.PostConstruct;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import local.sop.sopinfo.infrastructure.core.logging.CoreLoggingConfigurator;
import local.sop.sopinfo.infrastructure.core.logging.CoreLoggingProperties;

@AutoConfiguration
@PropertySource("classpath:application-core.properties")
@ConditionalOnClass(Logger.class)
@ConditionalOnProperty(prefix = "sopinfo.core.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(CoreLoggingProperties.class)
@AutoConfigureAfter(name = {
    "org.springframework.boot.autoconfigure.logging.LoggingAutoConfiguration"
})
public class CoreAutoConfiguration {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CoreAutoConfiguration.class);
    private final CoreLoggingProperties loggingProperties;

    public CoreAutoConfiguration(CoreLoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    @PostConstruct
    public void configureCoreLogging() {
        if (loggingProperties.isEnabled()) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            
            CoreLoggingConfigurator.configure(loggerContext, loggingProperties);
            
            if (loggingProperties.isDebugMode()) {
                log.info("🔧 SOPINFO Core Logging Auto-Configuration loaded");
            }
        }
    }

}
